package com.q.impl.socket;

import com.q.proto.RpcRequest;
import com.q.proto.RpcResponse;
import com.q.utils.ProtoUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
@Slf4j
public class SocketTransportThread implements Runnable {
    private Socket socket;
    private Object service;
    public SocketTransportThread(Socket socket, Object service) {
        this.socket=socket;
        this.service=service;
    }

    @Override
    public void run() {
        try (ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream())) {
            RpcRequest rpcRequest = (RpcRequest) objectInputStream.readObject();
            Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
            Object result = method.invoke(service, rpcRequest.getParameters());
            objectOutputStream.writeObject(ProtoUtils.successRpcResponse(result));
            objectOutputStream.flush();
        } catch (IOException | ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            log.error("调用或发送时有错误发生：", e);
        }
    }
}
