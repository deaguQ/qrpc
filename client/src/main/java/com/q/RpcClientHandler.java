package com.q;

import com.q.impl.netty.NettyTransportClient;
import com.q.impl.socket.SocketTransportClient;
import com.q.proto.RpcRequest;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class RpcClientHandler implements InvocationHandler {
    private boolean useNetty;
    public RpcClientHandler(boolean useNetty){
        this.useNetty=useNetty;
    }
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RpcRequest rpcRequest = RpcRequest.builder()
                .interfaceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .parameters(args)
                .paramTypes(method.getParameterTypes())
                .build();
        TransportClient transportClient;
        if(!useNetty){
//            transportClient=new SocketTransportClient();
            transportClient=null;
        }
        else{
            transportClient=new NettyTransportClient();
        }
        return transportClient.sendRequest(rpcRequest).getData();
    }
}
