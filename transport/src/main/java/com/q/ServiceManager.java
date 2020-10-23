package com.q;

import com.q.proto.RpcServiceDescriptor;

//本地服务注册与发现
public interface ServiceManager {
    <T> void register(T service,RpcServiceDescriptor toServiceDescriptor);

    Object getService(RpcServiceDescriptor toServiceDescriptor);
}
