package com.tengyue.im.socket;

import com.tengyue.im.model.SocketMessage;
import com.tengyue.im.util.MyJSONUtil;

import java.nio.channels.SelectionKey;

/**
 * Created by bingbing on 2017/8/9.
 */
public class MyMessageHandler implements IHandler {

    @Override
    public void messageReceived(SelectionKey selectionKey, String str) {

        try {
            SocketMessage message = MyJSONUtil.jsonToBean(str, SocketMessage.class);

            //客户端首次链接请求
            switch (message.getCmd()) {
                case StaticUtil.CONNECT:
                    //将链接句柄存储到集合里，方便查找，key是用户ID
                    ClientManager.getInstance().put(message.getFromId(), selectionKey);

                    //置为可写状态，发送离线消息等等……
                    selectionKey.interestOps(SelectionKey.OP_WRITE);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            ClientManager.getInstance().disconnect(selectionKey);
        }
    }

    @Override
    public void channelInactive(SelectionKey selectionKey) {
        ClientManager.getInstance().disconnect(selectionKey);
    }
}
