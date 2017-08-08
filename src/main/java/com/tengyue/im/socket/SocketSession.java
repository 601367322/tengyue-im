package com.tengyue.im.socket;

import java.nio.channels.SelectionKey;

/**
 * Created by bingbing on 2017/7/31.
 */
public class SocketSession {

    SelectionKey mSelectionKey;

    public SocketSession(SelectionKey selectionKey) {
        mSelectionKey = selectionKey;
    }

    public SelectionKey getSelectionKey() {
        return mSelectionKey;
    }
}
