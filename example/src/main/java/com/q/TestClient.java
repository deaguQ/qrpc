package com.q;

import com.q.service.HelloService;

public class TestClient {
    public static void main(String[] args) {
        RpcClient client = new RpcClient();
        //todo 注解自动注入
        //true时开启netty传输false时使用socket
        HelloService service = client.getProxy(HelloService.class,true);
        System.out.println(service.hello("hello server"));
        HelloService service2 = client.getProxy(HelloService.class,true,"impl2");
        System.out.println(service2.hello("hello server"));
    }
}
