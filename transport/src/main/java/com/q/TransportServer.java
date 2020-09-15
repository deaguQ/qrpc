package com.q;

import com.q.proto.RpcServiceDescriptor;

public interface TransportServer {
    void start();
    <T> void publishService(Object service, RpcServiceDescriptor serviceDescriptor);
}
