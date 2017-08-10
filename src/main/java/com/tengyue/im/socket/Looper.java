package com.tengyue.im.socket;

import org.apache.commons.lang.ArrayUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.tengyue.im.socket.SocketServer.getBundleFromSelectionKey;

/**
 * Created by bingbing on 2017/8/10.
 */
public class Looper implements Runnable {

    Selector mSelector;

    private int BLOCK = 50;

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

    public void processTaskQueue() {
        while (true) {
            final Runnable task = taskQueue.poll();
            if (task == null) {
                break;
            }
            task.run();
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
                final String receiveText = new String(receiveBuffer.array(), 0, count);

                System.out.println("receiveText:\t" + receiveText);

                for (int i = 0; i < mHandlers.size(); i++) {
                    mHandlers.get(i).messageReceived(selectionKey, receiveText);
                }
            } else if (count == -1) {
                for (int i = 0; i < mHandlers.size(); i++) {
                    mHandlers.get(i).channelInactive(selectionKey);
                }
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
                MessageBundle bundle = getBundleFromSelectionKey(selectionKey);
                if (!bundle.isEmpty()) {

                    byte[] msg = bundle.getFirst();

                    ByteBuffer sendBuffer = ByteBuffer.wrap(ArrayUtils.addAll(msg, "\n".getBytes()));

                    try {
                        //发送消息
                        int len = client.write(sendBuffer);
                        if (len == -1) {
                            //失败或异常，断开连接
                            ClientManager.getInstance().disconnect(selectionKey);
                            return;
                        }
                        bundle.removeFirst();
                    } catch (Exception e) {
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

    public void addHandler(IHandler handler) {
        mHandlers.add(handler);
    }
}
