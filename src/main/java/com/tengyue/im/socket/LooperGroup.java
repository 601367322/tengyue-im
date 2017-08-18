package com.tengyue.im.socket;

import com.tengyue.im.socket.handler.IHandler;

import java.nio.channels.SelectableChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by bingbing on 2017/8/10.
 */
public class LooperGroup {

    private static final int DEFAULT_EVENT_LOOP_THREADS;

    private ExecutorService mThreadPool;

    private List<Looper> mChildren = new ArrayList<>();
    private AtomicInteger mChildIndex = new AtomicInteger();

    static {
        DEFAULT_EVENT_LOOP_THREADS = Runtime.getRuntime().availableProcessors() * 2;
    }

    public LooperGroup() {
        mThreadPool = Executors.newFixedThreadPool(DEFAULT_EVENT_LOOP_THREADS);

        for (int i = 0; i < DEFAULT_EVENT_LOOP_THREADS; i++) {
            Looper looper = new Looper();
            mChildren.add(looper);
            mThreadPool.execute(looper);
        }
    }

    public void register(final SelectableChannel ch, final int interestOps) {
        try {
            next().register(ch, interestOps);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Looper next() {
        return mChildren.get(mChildIndex.getAndIncrement() % mChildren.size());
    }

    public void addHandler(IHandler handler) {
        for (int i = 0; i < mChildren.size(); i++) {
            mChildren.get(i).addHandler(handler);
        }
    }

    public void destroy() {
        mThreadPool.shutdownNow();
    }
}
