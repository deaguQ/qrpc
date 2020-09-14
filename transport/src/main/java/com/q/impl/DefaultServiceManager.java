package com.q.impl;

import com.q.exception.RpcException;
import com.q.message.RpcErrorMessage;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class DefaultServiceManager implements ServiceManager {

    private final static Map<String, Object> serviceMap = new ConcurrentHashMap<>();

    @Override
    public synchronized <T> void register(T service) {
        Class<?>[] interfaces = service.getClass().getInterfaces();
        if(interfaces.length == 0) {
            throw new RpcException(RpcErrorMessage.SERVICE_NOT_IMPLEMENT_ANY_INTERFACE);
        }
        for(Class<?> i : interfaces) {
            serviceMap.put(i.getCanonicalName(), service);
        }
    }

    @Override
    public synchronized Object getService(String serviceName) {
        Object service = serviceMap.get(serviceName);
        if(service == null) {
            throw new RpcException(RpcErrorMessage.SERVICE_CAN_NOT_BE_FOUND);
        }
        return service;
    }
}
