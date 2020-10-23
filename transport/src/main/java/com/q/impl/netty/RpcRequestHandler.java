package com.q.impl.netty;

import com.q.RequestHandler;
import com.q.exception.RpcException;
import com.q.factory.SingletonFactory;
import com.q.ServiceManager;
import com.q.proto.RpcRequest;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
@Slf4j
public class RpcRequestHandler {
    private static RequestHandler requestHandler;
    private static ServiceManager serviceManager;
    static {
        requestHandler = SingletonFactory.getInstance(RequestHandler.class);
        serviceManager = SingletonFactory.getInstance(DefaultServiceManager.class);
    }

    public Object handle(RpcRequest rpcRequest) {
        Object service=serviceManager.getService(rpcRequest.toServiceDescriptor());
        return invokeTargetMethod(rpcRequest,service);
    }
    private Object invokeTargetMethod(RpcRequest rpcRequest, Object service) {
        Object result;
        try {
            Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
            result = method.invoke(service, rpcRequest.getParameters());
            log.info("service:[{}] successful invoke method:[{}]", rpcRequest.getInterfaceName(), rpcRequest.getMethodName());
        } catch (NoSuchMethodException | IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
            throw new RpcException(e.getMessage(), e);
        }
        return result;
    }
}
