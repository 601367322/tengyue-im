package com.tengyue.im.socket;

import java.nio.channels.SelectionKey;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by bingbing on 2017/8/9.
 */
public class ClientManager {


    private ConcurrentHashMap<String, SelectionKey> mClients;

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

    public void put(String id, SelectionKey socketSession) {
        mClients.put(id, socketSession);
    }

    public SelectionKey get(String id){
        return mClients.get(id);
    }

    public void disconnect(SelectionKey selectionKey) {
        //客户端断开
        try {
            selectionKey.channel().close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            selectionKey.cancel();
        }

        //从Map中移除句柄
        Iterator<Map.Entry<String, SelectionKey>> iterator = mClients.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, SelectionKey> map = iterator.next();
            if (map.getValue() == selectionKey) {
                System.out.println("用户断开连接：\t" + map.getKey());
                iterator.remove();
                break;
            }
        }
    }
}
