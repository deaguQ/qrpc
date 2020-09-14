package com.q.rpc;

import java.net.InetSocketAddress;

public interface RpcRegistry {
    void register(String serviceName, InetSocketAddress inetSocketAddress);
    InetSocketAddress lookupService(String serviceName);
}
