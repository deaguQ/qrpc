package com.q;

import com.q.service.HelloService;

public class TestClient {
    public static void main(String[] args) {
        RpcClient client = new RpcClient("127.0.0.1", 8000);
        HelloService service = client.getProxy(HelloService.class);
        String s = service.hello("hello server");
        System.out.println(s);
    }
}
