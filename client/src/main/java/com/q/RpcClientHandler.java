package com.q;

import com.q.impl.socket.SocketTransportClient;
import com.q.proto.RpcRequest;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class RpcClientHandler implements InvocationHandler {
    private String host;
    private int port;
    public RpcClientHandler(String host,int port){
        this.host=host;
        this.port=port;
    }
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RpcRequest rpcRequest = RpcRequest.builder()
                .interfaceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .parameters(args)
                .paramTypes(method.getParameterTypes())
                .build();
        TransportClient transportClient=new SocketTransportClient();
        return transportClient.sendRequest(rpcRequest,host,port).getData();
    }
}
