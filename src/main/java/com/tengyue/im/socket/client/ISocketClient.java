package com.tengyue.im.socket.client;

/**
 * Created by bingbing on 2017/8/15.
 */
public interface ISocketClient {

    Object getChannel();

    void write(String msg);

    void disconnect();

    void waitWrite();
}
