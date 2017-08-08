package com.tengyue.im.model;

import net.sf.json.JSONObject;
import org.apache.commons.collections.map.HashedMap;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by bingbing on 2017/8/3.
 */
public class MyResponseBody implements Serializable {

    int errno = 0;

    String error = "";

    Object data;

    public MyResponseBody(int errno, String error, Object data) {
        this.errno = errno;
        this.error = error;
        this.data = data;
    }

    public MyResponseBody(Object data) {
        this.data = data;
    }

    public MyResponseBody(int errno, String error) {
        this.errno = errno;
        this.error = error;
        this.data = data;
    }

    public MyResponseBody() {
    }

    public int getErrno() {
        return errno;
    }

    public void setErrno(int errno) {
        this.errno = errno;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return JSONObject.fromObject(this).toString();
    }

    public static class Builder {

        private Map<String, Object> data;

        public Builder() {
            data = new HashedMap();
        }

        public Builder put(String key, Object value) {
            this.data.put(key, value);
            return this;
        }

        public MyResponseBody build() {
            return new MyResponseBody(data);
        }
    }
}
