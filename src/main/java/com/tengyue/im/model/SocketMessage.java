package com.tengyue.im.model;

import java.io.Serializable;

/**
 * Created by bingbing on 2017/8/3.
 */
public class SocketMessage implements Serializable{

    String cmd;
    String text;
    String fromId;
    String toId;

    public SocketMessage(String cmd, String text, String fromId, String toId) {
        this.cmd = cmd;
        this.text = text;
        this.fromId = fromId;
        this.toId = toId;
    }

    public SocketMessage() {
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getFromId() {
        return fromId;
    }

    public void setFromId(String fromId) {
        this.fromId = fromId;
    }

    public String getToId() {
        return toId;
    }

    public void setToId(String toId) {
        this.toId = toId;
    }
}
