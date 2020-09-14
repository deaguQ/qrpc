package com.q;

import com.q.service.HelloService;

public class TestClient {
    public static void main(String[] args) {
        //todo 负载均衡功能
        RpcClient client = new RpcClient();
        //todo 注解自动注入
        //true时开启netty传输
        HelloService service = client.getProxy(HelloService.class,true);
        String s = service.hello("hello server");
        System.out.println(s);
    }
}
