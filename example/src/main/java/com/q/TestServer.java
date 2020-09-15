package com.q;

import com.q.impl.annotations.ServiceScan;
import com.q.impl.netty.NettyTransportServer;
import com.q.service.HelloService;
import com.q.service.impl.HelloServiceImpl;
@ServiceScan
public class TestServer {
    public static void main(String[] args) {
        TransportServer transportServer=new NettyTransportServer("127.0.0.1",8000);
        transportServer.start();
    }
}
