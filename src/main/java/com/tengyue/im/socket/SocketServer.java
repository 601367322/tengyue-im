package com.tengyue.im.socket;

import com.tengyue.im.socket.handler.IHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * Created by bingbing on 2017/7/31.
 */
public class SocketServer extends Thread {

    private Selector selector;

    public static boolean start = true;

    private LooperGroup mLoopGroup;

    public SocketServer(int port) throws IOException {

        mLoopGroup = new LooperGroup();

        // 打开服务器套接字通道
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        // 服务器配置为非阻塞
        serverSocketChannel.configureBlocking(false);
        // 检索与此通道关联的服务器套接字
        ServerSocket serverSocket = serverSocketChannel.socket();
        // 进行服务的绑定
        serverSocket.bind(new InetSocketAddress(port));

        // 通过open()方法找到Selector
        selector = Selector.open();

        // 注册到selector，等待连接
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("Server Start----" + port);
    }

    @Override
    public void run() {
        try {
            while (start) {
                // 选择一组键，并且相应的通道已经打开
                selector.select();

                // 返回此选择器的已选择键集。
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();

                while (iterator.hasNext()) {

                    SelectionKey selectionKey = iterator.next();

                    //必须移除掉
                    iterator.remove();
                    handleKey(selectionKey);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void destroy() {
        mLoopGroup.destroy();
    }

    // 处理请求
    private void handleKey(SelectionKey selectionKey) throws IOException {

        // 测试此键的通道是否已准备好接受新的套接字连接。
        if (selectionKey.isAcceptable()) {
            ServerSocketChannel server = (ServerSocketChannel) selectionKey.channel();
            // 此方法返回的套接字通道（如果有）将处于阻塞模式。
            SocketChannel client = server.accept();
            // 配置为非阻塞
            client.configureBlocking(false);
            Socket socket = client.socket();
            socket.setTcpNoDelay(false);
            socket.setKeepAlive(true);

            // 注册到selector，等待连接
            mLoopGroup.register(client, SelectionKey.OP_READ);
        }
    }

    public void addHandler(IHandler handler) {
        mLoopGroup.addHandler(handler);
    }

}
