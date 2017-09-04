package com.tengyue.im.socket.handler;

import com.tengyue.im.model.SocketMessage;
import com.tengyue.im.socket.client.ClientManager;
import com.tengyue.im.socket.client.ISocketClient;
import com.tengyue.im.socket.client.SocketClient;
import com.tengyue.im.util.MyJSONUtil;
import com.tengyue.im.util.StaticUtil;
import org.springframework.web.socket.WebSocketSession;

import java.nio.channels.SelectionKey;

/**
 * Created by bingbing on 2017/8/9.
 */
public class MyMessageHandler implements IHandler {

    @Override
    public void messageReceived(Object session, String str) {

        try {
            SocketMessage message = MyJSONUtil.jsonToBean(str, SocketMessage.class);

            //客户端首次链接请求
            switch (message.getCmd()) {
                case StaticUtil.CONNECT:
                    ISocketClient client = generateClient(session);

                    //将链接句柄存储到集合里，方便查找，key是用户ID
                    ClientManager.getInstance().put(message.getFromId(), generateClient(session));

                    client.waitWrite();
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            ClientManager.getInstance().disconnect(session);
        }
    }

    ISocketClient generateClient(Object session) {
        if (session instanceof WebSocketSession) {
            return null;
        } else {
            return new SocketClient((SelectionKey) session);
        }
    }
}
