package com.q;

import com.q.impl.annotations.ServiceScan;
import com.q.impl.netty.NettyTransportServerAbstract;
import com.q.service.HelloService;
import com.q.service.impl.HelloServiceImpl;
@ServiceScan
public class TestServer {
    public static void main(String[] args) {
        //todo 注解注册本地服务
//        TransportServer transportServer = new SocketTransportServer(serviceManager);
        TransportServer transportServer=new NettyTransportServerAbstract("127.0.0.1",8000);
        transportServer.start();
    }
}
