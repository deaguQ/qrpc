package com.q;

import com.q.message.RpcErrorMessage;
import com.q.proto.RpcRequest;
import com.q.proto.RpcResponse;
import com.q.utils.ProtoUtils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.spi.ServiceRegistry;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;

@Slf4j
public class RequestHandler {



    public Object handle(RpcRequest rpcRequest, Object service) {
        Object result = null;
        try {
            result = invokeTargetMethod(rpcRequest, service);
            log.info("服务:{} 成功调用方法:{}", rpcRequest.getInterfaceName(), rpcRequest.getMethodName());
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.error("调用或发送时有错误发生：", e);
        } return result;
    }
    private Object invokeTargetMethod(RpcRequest rpcRequest, Object service) throws IllegalAccessException, InvocationTargetException {
        Method method;
        try {
            method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
        } catch (NoSuchMethodException e) {
            return ProtoUtils.failRpcResponse(RpcErrorMessage.METHOD_NOT_FOUND.getMessage());
        }
        return method.invoke(service, rpcRequest.getParameters());
    }
}
