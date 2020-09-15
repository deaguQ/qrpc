package com.q.rpc;

import com.q.proto.RpcServiceDescriptor;

import java.net.InetSocketAddress;

public interface RpcRegistry {
    InetSocketAddress lookupService(RpcServiceDescriptor rpcServiceDescriptor);
    void publishService(RpcServiceDescriptor rpcServiceDescriptor,InetSocketAddress inetSocketAddress);
}
