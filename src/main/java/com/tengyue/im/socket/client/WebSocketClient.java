package com.tengyue.im.socket.client;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

/**
 * Created by bingbing on 2017/8/15.
 */
public class WebSocketClient implements ISocketClient {

    WebSocketSession mSocketSession;

    public WebSocketClient(WebSocketSession socketSession) {
        mSocketSession = socketSession;
    }

    @Override
    public Object getChannel() {
        return mSocketSession;
    }

    @Override
    public void write(String msg) {
        try {
            mSocketSession.sendMessage(new TextMessage(msg.getBytes()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void disconnect() {
        try {
            if (mSocketSession.isOpen()) {
                mSocketSession.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void waitWrite() {
        //do nothing
    }
}
