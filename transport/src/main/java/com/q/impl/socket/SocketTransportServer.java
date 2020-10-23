package com.q.impl.socket;

import com.q.RequestHandler;
import com.q.ServiceManager;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

@Slf4j
//负责接收连接并调用本地服务返回数据
public class SocketTransportServer  {
    private final ExecutorService threadPool;
    private ServiceManager serviceManager;
    private RequestHandler requestHandler=new RequestHandler();
    public SocketTransportServer(ServiceManager serviceManager){
        int corePoolSize = 5;
        int maximumPoolSize = 50;
        long keepAliveTime = 60;
        BlockingQueue<Runnable> workingQueue = new ArrayBlockingQueue<>(100);
        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        threadPool = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS, workingQueue, threadFactory);
        this.serviceManager=serviceManager;
    }
//    @Override
    public void start(int port){
        try (ServerSocket server = new ServerSocket(port)) {
            log.info("server start [{}]",port);
            Socket socket;
            while ((socket = server.accept()) != null) {
                log.info("client connected [{}]", socket.getInetAddress());
                threadPool.execute(new SocketTransportThread(socket,serviceManager,requestHandler));
            }
            threadPool.shutdown();
        } catch (IOException e) {
            log.error("occur IOException:", e);
        }
    }

//    @Override
    public <T> void publishService(Object service, Class<T> serviceClass) {

    }
}
