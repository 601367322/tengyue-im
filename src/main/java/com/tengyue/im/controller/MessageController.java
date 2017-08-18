package com.tengyue.im.controller;

import com.tengyue.im.exception.MyException;
import com.tengyue.im.model.MyResponseBody;
import com.tengyue.im.socket.client.ClientManager;
import com.tengyue.im.socket.client.ISocketClient;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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
        ISocketClient socketClient = ClientManager.getInstance().get(toId);
        //是否可用
        if (socketClient != null) {
            socketClient.write(text);
        } else {
            throw new MyException("对方不在线");
        }

        return new MyResponseBody();
    }
}