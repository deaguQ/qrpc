package com.q.impl.socket;

import com.q.RequestHandler;
import com.q.impl.ServiceManager;
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
    private ServiceManager serviceManager;
    private RequestHandler requestHandler;
    public SocketTransportThread(Socket socket, ServiceManager serviceManager, RequestHandler requestHandler) {
        this.socket=socket;
        this.serviceManager=serviceManager;
        this.requestHandler=requestHandler;
    }

    @Override
    public void run() {
        try (ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream())) {
            RpcRequest rpcRequest = (RpcRequest) objectInputStream.readObject();
            Object service=serviceManager.getService(rpcRequest.getInterfaceName());
            Object result = requestHandler.handle(rpcRequest, service);
            objectOutputStream.writeObject(ProtoUtils.successRpcResponse(result));
            objectOutputStream.flush();
        } catch (IOException | ClassNotFoundException e) {
            log.error("调用或发送时有错误发生：", e);
        }
    }
}
