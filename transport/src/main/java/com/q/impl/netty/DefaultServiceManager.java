package com.q.impl.netty;

import com.q.ServiceManager;
import com.q.exception.RpcException;
import com.q.message.RpcErrorMessage;
import com.q.proto.RpcServiceDescriptor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 管理本地服务的容器
 */
@Slf4j
public class DefaultServiceManager implements ServiceManager {

    private final static Map<String, Object> serviceMap = new ConcurrentHashMap<>();

    @Override
    public <T> void register(T service, RpcServiceDescriptor serviceDescriptor) {
        String rpcServiceName = serviceDescriptor.toRpcServiceName();
        if (serviceMap.containsKey(rpcServiceName)) {
            return;
        }
        serviceMap.put(rpcServiceName, service);
        log.info("Add service: {} and interfaces:{}", rpcServiceName, service.getClass().getInterfaces());
    }

    @Override
    public Object getService(RpcServiceDescriptor serviceDescriptor) {
        Object service = serviceMap.get(serviceDescriptor.toRpcServiceName());
        if (null == service) {
            throw new RpcException(RpcErrorMessage.SERVICE_CAN_NOT_BE_FOUND);
        }
        return service;
    }
}
