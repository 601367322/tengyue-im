package com.tengyue.im.controller;

import com.tengyue.im.exception.MyException;
import com.tengyue.im.model.MyResponseBody;
import com.tengyue.im.socket.MessageBundle;
import com.tengyue.im.socket.SocketServer;
import com.tengyue.im.socket.SocketSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.nio.channels.SelectionKey;

/**
 * Created by bingbing on 2017/8/2.
 */
@Controller
@RequestMapping(value = "/msg")
public class MessageController {

    @RequestMapping(value = "/send")
    public
    @ResponseBody
    MyResponseBody sendMsg(
            @RequestParam String text,
            @RequestParam String fromId,
            @RequestParam String toId
    ) throws MyException {

        System.out.println("发送者：\t" + fromId);
        System.out.println("接受者：\t" + toId);
        System.out.println("消息：\t" + text);

        //得到对方的Session
        SocketSession mSession = SocketServer.clients.get(toId);
        if (mSession != null) {
            //是否可用
            if (mSession.getSelectionKey().isValid()) {
                //将消息加入队列
                MessageBundle bundle = SocketServer.getBundleFromSelectionKey(mSession.getSelectionKey());
                bundle.enqueue(text);

                //变为可写状态
                mSession.getSelectionKey().attach(bundle);
                mSession.getSelectionKey().interestOps(SelectionKey.OP_WRITE);
                mSession.getSelectionKey().selector().wakeup();
            }
        } else {
            throw new MyException("对方不在线");
        }

        return new MyResponseBody();
    }
}