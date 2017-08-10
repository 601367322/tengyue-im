package com.tengyue.im.socket;

import java.nio.channels.SelectionKey;

/**
 * Created by bingbing on 2017/8/9.
 */
public interface IHandler {

    void messageReceived(SelectionKey selectionKey,String message);

    void channelInactive(SelectionKey selectionKey);
}
