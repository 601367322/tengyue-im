package com.tengyue.im.exception;

/**
 * Created by Shen on 2015/12/26.
 */
public class MyException extends Exception {

    int code = -1;

    public MyException() {
        super();
    }

    public MyException(String msg) {
        super(msg);
    }

    public MyException(int code, String msg) {
        super(msg);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
