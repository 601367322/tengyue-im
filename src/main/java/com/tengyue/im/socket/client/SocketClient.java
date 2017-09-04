package com.tengyue.im.socket.client;

import java.io.Serializable;
import java.nio.channels.SelectionKey;
import java.util.ArrayList;

/**
 * Created by bingbing on 2017/8/15.
 */
public class SocketClient implements ISocketClient {

    SelectionKey mSelectionKey;

    public SocketClient(SelectionKey selectionKey) {
        mSelectionKey = selectionKey;
    }

    @Override
    public Object getChannel() {
        return mSelectionKey;
    }

    @Override
    public void write(String msg) {
        //将消息加入队列
        MessageBundle bundle = getBundleFromSelectionKey(mSelectionKey);
        bundle.enqueue(msg);

        //变为可写状态
        mSelectionKey.attach(bundle);
        mSelectionKey.interestOps(SelectionKey.OP_WRITE);
        mSelectionKey.selector().wakeup();
    }

    @Override
    public void disconnect() {
        //客户端断开
        try {
            mSelectionKey.channel().close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mSelectionKey.cancel();
        }
    }

    @Override
    public void waitWrite() {
        mSelectionKey.interestOps(SelectionKey.OP_WRITE);
    }

    public static class MessageBundle implements Serializable {

        ArrayList<String> messageQueue = new ArrayList<>();

        public void enqueue(String str) {
            messageQueue.add(str);
        }

        public String getFirst() {
            if (messageQueue.size() > 0)
                return messageQueue.get(0);
            else
                return null;
        }

        public boolean isEmpty() {
            return messageQueue.size() == 0;
        }

        public void removeFirst() {
            if (!isEmpty())
                messageQueue.remove(0);
        }
    }


    public static SocketClient.MessageBundle getBundleFromSelectionKey(SelectionKey key) {
        SocketClient.MessageBundle bundle = (SocketClient.MessageBundle) key.attachment();
        if (bundle == null) {
            bundle = new SocketClient.MessageBundle();
        }
        return bundle;
    }

}
