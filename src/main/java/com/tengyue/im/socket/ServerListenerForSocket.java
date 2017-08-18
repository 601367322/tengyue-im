package com.tengyue.im.socket;

import com.tengyue.im.socket.handler.MyMessageHandler;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Component
public class ServerListenerForSocket {

    private static final int PORT = 8083;
    SocketServer mServer = null;

    @PostConstruct
    public void init() {
        try {
            mServer = new SocketServer(PORT);
            mServer.addHandler(new MyMessageHandler());
            mServer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @PreDestroy
    public void destroy() {
        SocketServer.start = false;
        mServer.destroy();
    }

}
