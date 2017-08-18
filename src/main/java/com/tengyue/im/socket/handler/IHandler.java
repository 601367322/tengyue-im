package com.tengyue.im.socket.handler;

/**
 * Created by bingbing on 2017/8/9.
 */
public interface IHandler {

    void messageReceived(Object session, String message);

}
