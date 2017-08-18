package com.tengyue.im.socket.client;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by bingbing on 2017/8/9.
 */
public class ClientManager {


    private ConcurrentHashMap<String, ISocketClient> mClients;

    private static ClientManager mInstance;

    public static ClientManager getInstance() {
        if (mInstance == null) {
            synchronized (ClientManager.class) {
                if (mInstance == null) {
                    mInstance = new ClientManager();
                }
            }
        }
        return mInstance;
    }

    private ClientManager() {
        mClients = new ConcurrentHashMap();
    }

    public void put(String id, ISocketClient socketSession) {
        mClients.put(id, socketSession);
    }

    public ISocketClient get(String id) {
        return mClients.get(id);
    }

    public ISocketClient get(Object value) {
        //从Map中移除句柄
        Iterator<Map.Entry<String, ISocketClient>> iterator = mClients.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, ISocketClient> map = iterator.next();
            if (map.getValue().getChannel() == value) {
                return map.getValue();
            }
        }
        return null;
    }

    public void disconnect(Object value) {
        //从Map中移除句柄
        Iterator<Map.Entry<String, ISocketClient>> iterator = mClients.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, ISocketClient> map = iterator.next();
            if (map.getValue().getChannel() == value) {
                ISocketClient client = map.getValue();
                iterator.remove();
                client.disconnect();
                System.out.println("用户断开连接：\t" + map.getKey());
                break;
            }
        }
    }

}
