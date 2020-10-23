package com.q;

import com.q.factory.SingletonFactory;
import com.q.impl.netty.NettyTransportClient;
import com.q.impl.socket.SocketTransportClient;
import com.q.proto.RpcRequest;
import com.q.proto.RpcResponse;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class RpcClientHandler implements InvocationHandler {
    private boolean useNetty;
    private String group;
    private static TransportClient nettyClient= SingletonFactory.getInstance(NettyTransportClient.class);
    public RpcClientHandler(boolean useNetty,String group){
        this.useNetty=useNetty;
        this.group=group;
    }
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RpcRequest rpcRequest = RpcRequest.builder()
                .interfaceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .parameters(args)
                .paramTypes(method.getParameterTypes())
                .requestId(UUID.randomUUID().toString())
                .group(group)
                .build();
        TransportClient transportClient;
        if(!useNetty){
            //todo socket
            transportClient=null;
        }
        else{
            transportClient=nettyClient;
        }
        CompletableFuture<RpcResponse> completableFuture = (CompletableFuture<RpcResponse>) transportClient.sendRequest(rpcRequest);
        return completableFuture.get().getData();
    }
}
