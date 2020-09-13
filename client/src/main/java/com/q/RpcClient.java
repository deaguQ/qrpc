package com.q;


import java.lang.reflect.Proxy;

//获取代理类
public class RpcClient{
    private String host;
    private int port;
    public RpcClient(String host, int port) {
        this.host = host;
        this.port = port;
    }
    public <T> T getProxy(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, new RpcClientHandler(host,port));
    }
}
