package com.q.impl;

import com.q.proto.RpcServiceDescriptor;

//用来管理本地服务
public interface ServiceManager {
    <T> void register(T service,RpcServiceDescriptor toServiceDescriptor);

    Object getService(RpcServiceDescriptor toServiceDescriptor);
}
