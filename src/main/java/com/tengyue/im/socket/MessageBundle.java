package com.tengyue.im.socket;

import java.util.ArrayList;

/**
 * Created by bingbing on 2017/8/1.
 */
public class MessageBundle {

    ArrayList<byte[]> messageQueue = new ArrayList<>();

    public void enqueue(String str) {
        messageQueue.add(str.getBytes());
    }

    public byte[] getFirst() {
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
