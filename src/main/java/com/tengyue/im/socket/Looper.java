package com.tengyue.im.socket;

import com.tengyue.im.socket.client.ClientManager;
import com.tengyue.im.socket.client.SocketClient;
import com.tengyue.im.socket.handler.IHandler;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.tengyue.im.socket.client.SocketClient.getBundleFromSelectionKey;

/**
 * Created by bingbing on 2017/8/10.
 */
public class Looper implements Runnable {

    Selector mSelector;

    private int BLOCK = 1024;

    private List<IHandler> mHandlers = new ArrayList<>();

    private final Queue<Runnable> taskQueue = new ConcurrentLinkedQueue<>();

    private final AtomicBoolean wakenUp = new AtomicBoolean();

    public Looper() {
        try {
            mSelector = Selector.open();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void register(final SelectableChannel ch, final int interestOps) {
        try {
            Runnable task = () -> {
                try {
                    ch.register(mSelector, interestOps);
                } catch (ClosedChannelException e) {
                    e.printStackTrace();
                }
            };
            taskQueue.add(task);

            if (mSelector != null) {
                if (wakenUp.compareAndSet(false, true)) {
                    mSelector.wakeup();
                }
            } else {
                taskQueue.remove(task);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            while (SocketServer.start) {

                wakenUp.set(false);

                // 选择一组键，并且相应的通道已经打开
                mSelector.select();

                processTaskQueue();

                // 返回此选择器的已选择键集。
                Iterator<SelectionKey> iterator = mSelector.selectedKeys().iterator();

                while (iterator.hasNext()) {

                    SelectionKey selectionKey = iterator.next();

                    //必须移除掉
                    iterator.remove();
                    handleKey(selectionKey);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // 处理请求
    private void handleKey(SelectionKey selectionKey) throws IOException {
        if (selectionKey.isReadable()) {
            // 返回为之创建此键的通道。
            SocketChannel client = (SocketChannel) selectionKey.channel();
            //将缓冲区清空以备下次读取
            ByteBuffer receiveBuffer = ByteBuffer.allocate(BLOCK);
            //读取服务器发送来的数据到缓冲区中
            int count = client.read(receiveBuffer);

            if (count > 0) {
                final String receiveText = WSP13Decode(receiveBuffer.array());

                System.out.println("receiveText:\t" + receiveText.trim());

                if (receiveText.startsWith("GET / HTTP/1.1")) {
                    if (receiveText.contains("Sec-WebSocket-Key")) {
                        String[] strs = receiveText.split("\r\n");

                        for (int i = 0; i < strs.length; i++) {
                            String key = strs[i];
                            if (key.contains("Sec-WebSocket-Key")) {
                                try {
                                    //握手
                                    //通过字符串截取获取key值
                                    key = key.substring(0, key.indexOf("==") + 2);
                                    key = key.substring(key.indexOf("Key") + 4, key.length()).trim();
                                    //拼接WEBSOCKET传输协议的安全校验字符串
                                    key += "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
                                    //通过SHA-1算法进行更新
                                    MessageDigest md = MessageDigest.getInstance("SHA-1");
                                    md.update(key.getBytes("utf-8"), 0, key.length());
                                    byte[] sha1Hash = md.digest();
                                    //进行Base64加密
                                    sun.misc.BASE64Encoder encoder = new sun.misc.BASE64Encoder();
                                    key = encoder.encode(sha1Hash);

                                    StringBuilder builder = new StringBuilder();

                                    //服务器端返回输出内容
                                    builder.append("HTTP/1.1 101 Web Socket Protocol Handshake\r\n");
                                    builder.append("Connection: Upgrade\r\n");
                                    builder.append("Date: "+getServerTime()+"\r\n");
                                    builder.append("Sec-WebSocket-Accept: " + key + "\r\n");
                                    builder.append("Server: TooTallNate Java-WebSocket\r\n");
                                    builder.append("Upgrade: websocket\r\n\r\n");

                                    new SocketClient(selectionKey).write(builder.toString());
                                } catch (NoSuchAlgorithmException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        return;
                    }
                } else {
                    for (int i = 0; i < mHandlers.size(); i++) {
                        mHandlers.get(i).messageReceived(selectionKey, receiveText);
                    }
                }
            } else if (count == -1) {
                ClientManager.getInstance().disconnect(selectionKey);
            }
        } else if (selectionKey.isWritable()) {

            SocketChannel client = (SocketChannel) selectionKey.channel();

            try {
                System.out.println("write:\n" + client.getRemoteAddress());
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (selectionKey.attachment() != null) {

                //从SelectionKey中得到需要发送的消息
                SocketClient.MessageBundle bundle = getBundleFromSelectionKey(selectionKey);

                if (!bundle.isEmpty()) {

                    byte[] msg;
                    ByteBuffer sendBuffer;

                    if (bundle.getFirst().startsWith("HTTP/1.1")) {
                        msg = bundle.getFirst().getBytes();
                        sendBuffer = ByteBuffer.wrap(msg);
                    } else {
                        sendBuffer = createBinaryFrame(ByteBuffer.wrap(bundle.getFirst().getBytes()));
                    }

                    try {

                        //发送消息
                        int len = client.write(sendBuffer);

                        sendBuffer.flip();

                        if (len == -1) {
                            //失败或异常，断开连接
                            ClientManager.getInstance().disconnect(selectionKey);
                            return;
                        }

                        bundle.removeFirst();
                    } catch (Exception e) {
                        e.printStackTrace();
                        ClientManager.getInstance().disconnect(selectionKey);
                    } finally {
                        if (!bundle.isEmpty()) {
                            //如果消息为空，则转为可读状态
                            selectionKey.interestOps(SelectionKey.OP_WRITE);
                            return;
                        }
                    }
                }
            }


            selectionKey.interestOps(SelectionKey.OP_READ);
        }

    }

    private String getServerTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "EEE, dd MMM yyyy HH:mm:ss z", Locale.US );
        dateFormat.setTimeZone( TimeZone.getTimeZone( "GMT" ) );
        return dateFormat.format( calendar.getTime() );
    }

    public ByteBuffer createBinaryFrame( ByteBuffer framedata ) {
        ByteBuffer mes = framedata;
        boolean mask = false; // framedata.getTransfereMasked();
        int sizebytes = mes.remaining() <= 125 ? 1 : mes.remaining() <= 65535 ? 2 : 8;
        ByteBuffer buf = ByteBuffer.allocate( 1 + ( sizebytes > 1 ? sizebytes + 1 : sizebytes ) + ( mask ? 4 : 0 ) + mes.remaining() );
        byte optcode = 1;
        byte one = ( byte ) (-128);
        one |= optcode;
        buf.put( one );
        byte[] payloadlengthbytes = toByteArray( mes.remaining(), sizebytes );
        assert ( payloadlengthbytes.length == sizebytes );

        if( sizebytes == 1 ) {
            buf.put( ( byte ) ( payloadlengthbytes[0] | ( mask ? ( byte ) -128 : 0 ) ) );
        } else if( sizebytes == 2 ) {
            buf.put( ( byte ) ( ( byte ) 126 | ( mask ? ( byte ) -128 : 0 ) ) );
            buf.put( payloadlengthbytes );
        } else if( sizebytes == 8 ) {
            buf.put( ( byte ) ( ( byte ) 127 | ( mask ? ( byte ) -128 : 0 ) ) );
            buf.put( payloadlengthbytes );
        } else
            throw new RuntimeException( "Size representation not supported/specified" );

        if( mask ) {
            ByteBuffer maskkey = ByteBuffer.allocate( 4 );
            maskkey.putInt( 1 );
            buf.put( maskkey.array() );
            for( int i = 0; mes.hasRemaining(); i++ ) {
                buf.put( ( byte ) ( mes.get() ^ maskkey.get( i % 4 ) ) );
            }
        } else
            buf.put( mes );
        // translateFrame ( buf.array () , buf.array ().length );
        assert ( buf.remaining() == 0 ) : buf.remaining();
        buf.flip();

        return buf;
    }



    private byte[] toByteArray(long val, int bytecount) {
        byte[] buffer = new byte[bytecount];
        int highest = 8 * bytecount - 8;
        for (int i = 0; i < bytecount; i++) {
            buffer[i] = (byte) (val >>> (highest - 8 * i));
        }
        return buffer;
    }

    public static String WSP13Decode(byte[] data) {
        byte _firstByte = data[0];
        byte _secondByte = data[1];
        int opcode = _firstByte & 0x0F;
        boolean isMasked = ((_firstByte & 128) == 128);
        // 实载数据长度
        int _payloadSize = _secondByte & 0x7F;

        if (!isMasked || opcode != 1) {
            try {
                return new String(data, "utf-8").trim();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } // not masked and opcode text

        int[] mask = new int[4];
        for (int i = 2; i < 6; i++) {
            mask[i - 2] = data[i];
        }

        int _payloadOffset = 6;
        int dataLength = _payloadSize + _payloadOffset;

        int _payload_int_Length = dataLength - _payloadOffset;
        int[] _payload_int = new int[_payload_int_Length];
        for (int i = _payloadOffset; i < dataLength; i++) {
            int j = i - _payloadOffset;

            int _unmaskPL = data[i] ^ mask[j % 4];
            _payload_int[j] = _unmaskPL;
        }

        byte[] _payload_byte = new byte[_payload_int.length];

        for (int i = 0; i < _payload_int.length; i++) {

            byte _eachByte = (byte) (0xff & _payload_int[i]);

            _payload_byte[i] = _eachByte;
        }

        String _result = new String(_payload_byte);

        return _result.trim();

    }

    public static byte[] WSP13Encode(String data) {

        // 一次最多127k，内容就只有125k，协议头2k
        if (data.length() > 125) {
            data = data.substring(0, 125);
        }

        byte[] _payload_byte = null;
        try {
            _payload_byte = data.getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        // 只使用了32位int的后8位,作为head中每个字节,因为之后 % 4 了
        // 实载数据长度
        int _payload_length = _payload_byte.length;
        int _first4Byte = 129; // 1000 0001, fin and opcode
        int _second4Byte = _payload_length + 128; // 1000 0000, mask,
        // 第一位是1(mask位),所以后面需要mask
        // key作为安全需要
        int _head_FirstPart_length = 2;
        int _mask_length = 4;
        int _head_length = _payload_length + _mask_length
                + _head_FirstPart_length;
        // head's byte
        int[] _head = new int[_head_length];
        // mask's byte
        int[] _mask = new int[_mask_length];

        _head[0] = _first4Byte;
        _head[1] = _second4Byte;

        int _time_ms = (int) System.currentTimeMillis();
        // mask是个随机数(位),用来加密
        for (int i = 0; i < 4; i++) {
            _mask[i] = _time_ms % 255;
        }
        // 把mask key放进head
        for (int i = 0, j = _head_FirstPart_length; i < _mask.length; i++, j++) {
            _head[j] = _mask[i];
        }

        for (int i = 0, j = _mask_length + _head_FirstPart_length; i < _payload_length; i++, j++) {
            _head[j] = _payload_byte[i] ^ _mask[i % 4];
        }

        byte[] _payload_byte_protocol = new byte[_head_length];

        for (int i = 0; i < _head_length; i++) {
            _payload_byte_protocol[i + 0] = (byte) (0xff & _head[i]);
        }

        String _result = new String(_payload_byte_protocol);
        System.out.println("pppp:" + _result + " " + _result.length());

        return _payload_byte_protocol;

    }

    public void processTaskQueue() {
        while (true) {
            final Runnable task = taskQueue.poll();
            if (task == null) {
                break;
            }
            task.run();
        }
    }

    public void addHandler(IHandler handler) {
        mHandlers.add(handler);
    }
}
