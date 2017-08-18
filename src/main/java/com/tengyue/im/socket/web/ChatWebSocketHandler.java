package com.tengyue.im.socket.web;

import com.tengyue.im.socket.handler.IHandler;
import com.tengyue.im.socket.handler.MyMessageHandler;
import com.tengyue.im.socket.client.ClientManager;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class ChatWebSocketHandler extends TextWebSocketHandler {

    IHandler handler = new MyMessageHandler();

    //接收文本消息，并发送出去
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        super.handleTextMessage(session, message);

        handler.messageReceived(session, message.getPayload());
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        //处理离线消息
    }

    //抛出异常时处理
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        ClientManager.getInstance().disconnect(session);
    }

    //连接关闭后处理
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        ClientManager.getInstance().disconnect(session);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}