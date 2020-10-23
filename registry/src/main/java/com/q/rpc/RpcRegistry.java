package com.q.rpc;

import com.q.proto.RpcServiceDescriptor;

import java.net.InetSocketAddress;
//用来远程发布订阅服务
public interface RpcRegistry {
    InetSocketAddress lookupService(RpcServiceDescriptor rpcServiceDescriptor);
    void publishService(RpcServiceDescriptor rpcServiceDescriptor,InetSocketAddress inetSocketAddress);
}
