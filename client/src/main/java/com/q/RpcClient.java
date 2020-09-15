package com.q;


import java.lang.reflect.Proxy;

//获取代理类
public class RpcClient{

    public <T> T getProxy(Class<T> clazz,boolean useNetty) {
        return getProxy(clazz,useNetty,"");
    }
    public <T> T getProxy(Class<T> clazz,boolean useNetty,String group) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, new RpcClientHandler(useNetty,group));
    }
}
