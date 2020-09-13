package com.q;

import com.q.impl.socket.SocketTransportClient;
import com.q.impl.socket.SocketTransportServer;
import com.q.service.impl.HelloServiceImpl;

public class TestServer {
    public static void main(String[] args) {
        SocketTransportServer transportServer = new SocketTransportServer();
        transportServer.registerService(new HelloServiceImpl());
        transportServer.start(8000);
    }
}
