package com.tengyue.im.socket;

import com.tengyue.im.model.SocketMessage;
import com.tengyue.im.util.MyJSONUtil;
import org.apache.commons.lang.ArrayUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by bingbing on 2017/7/31.
 */
public class SocketServer extends Thread {
    /*缓冲区大小*/
    private int BLOCK = 50;

    private ByteBuffer receiveBuffer = ByteBuffer.allocate(BLOCK);

    private Selector selector;

    public static ConcurrentHashMap<String, SocketSession> clients = new ConcurrentHashMap();

    private boolean start = true;

    private ExecutorService mThreadAcceptPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);
    private ExecutorService mThreadReadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);
    private ExecutorService mThreadWritePool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);


    public SocketServer(int port) throws IOException {
        // 打开服务器套接字通道
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        // 服务器配置为非阻塞
        serverSocketChannel.configureBlocking(false);
        // 检索与此通道关联的服务器套接字
        ServerSocket serverSocket = serverSocketChannel.socket();
        // 进行服务的绑定
        serverSocket.bind(new InetSocketAddress(port));
        // 通过open()方法找到Selector
        selector = Selector.open();
        // 注册到selector，等待连接
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("Server Start----" + port);
    }

    @Override
    public void run() {
        try {
            while (start) {
                // 选择一组键，并且相应的通道已经打开
                selector.select();

                // 返回此选择器的已选择键集。
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();

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
        // 接受请求
        // 接受到此通道套接字的连接。
        ServerSocketChannel server = null;
        SocketChannel client = null;

        // 测试此键的通道是否已准备好接受新的套接字连接。
        if (selectionKey.isAcceptable()) {
            server = (ServerSocketChannel) selectionKey.channel();
            // 此方法返回的套接字通道（如果有）将处于阻塞模式。
            client = server.accept();
            // 配置为非阻塞
            client.configureBlocking(false);
            // 注册到selector，等待连接
            client.register(selector, SelectionKey.OP_READ);
        } else if (selectionKey.isReadable()) {
            // 返回为之创建此键的通道。
            client = (SocketChannel) selectionKey.channel();
            //将缓冲区清空以备下次读取
            receiveBuffer.clear();
            //读取服务器发送来的数据到缓冲区中
            int count = client.read(receiveBuffer);
            if (count > 0) {
                String receiveText = new String(receiveBuffer.array(), 0, count);
                System.out.println("服务器端接受客户端数据--:" + receiveText);

                try {
                    SocketMessage message = MyJSONUtil.jsonToBean(receiveText, SocketMessage.class);

                    //客户端首次链接请求
                    switch (message.getCmd()) {
                        case StaticUtil.CONNECT:
                            //将链接句柄存储到集合里，方便查找，key是用户ID
                            clients.put(message.getFromId(), new SocketSession(selectionKey));

                            //置为可写状态，发送离线消息等等……
                            selectionKey.interestOps(SelectionKey.OP_WRITE);
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if (count == -1) {
                //客户端主动断开时，销毁服务端链接
                disconnectClick(selectionKey);

            }
        } else if (selectionKey.isWritable()) {
            client = (SocketChannel) selectionKey.channel();

            if (selectionKey.attachment() != null) {

                //从SelectionKey中得到需要发送的消息
                MessageBundle bundle = getBundleFromSelectionKey(selectionKey);
                if (!bundle.isEmpty()) {

                    byte[] msg = bundle.getFirst();

                    ByteBuffer sendBuffer = ByteBuffer.wrap(ArrayUtils.addAll(msg,"\n".getBytes()));

                    try {
                        //发送消息
                        int len = client.write(sendBuffer);
                        if (len == -1) {
                            //失败或异常，断开连接
                            disconnectClick(selectionKey);
                            return;
                        }
                        bundle.removeFirst();
                    } catch (Exception e) {
                        disconnectClick(selectionKey);
                    } finally {
                        if (bundle.isEmpty()) {
                            //如果消息为空，则转为可读状态
                            selectionKey.interestOps(SelectionKey.OP_READ);
                        }
                    }
                }
            } else {
                //如果消息为空，则转为可读状态
                selectionKey.interestOps(SelectionKey.OP_READ);
            }
        }
    }

    public static MessageBundle getBundleFromSelectionKey(SelectionKey key) {
        MessageBundle bundle = (MessageBundle) key.attachment();
        if (bundle == null) {
            bundle = new MessageBundle();
        }
        return bundle;
    }

    private void disconnectClick(SelectionKey selectionKey) throws IOException {
        //客户端断开
        try {
            selectionKey.channel().close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            selectionKey.cancel();
        }

        //从Map中移除句柄
        Iterator<Map.Entry<String, SocketSession>> iterator = clients.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, SocketSession> map = iterator.next();
            if (map.getValue().getSelectionKey() == selectionKey) {
                System.out.println("用户断开连接：\t" + map.getKey());
                iterator.remove();
                break;
            }
        }
    }

    public void setStart(boolean start) {
        this.start = start;
    }
}
