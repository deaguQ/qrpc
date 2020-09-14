package com.q.impl.netty;

import com.q.ShutdownHook;
import com.q.exception.RpcException;
import com.q.impl.AbstractAutoTransportServer;
import com.q.impl.DefaultServiceManager;
import com.q.impl.ServiceManager;
import com.q.impl.annotations.Service;
import com.q.impl.annotations.ServiceScan;
import com.q.message.RpcErrorMessage;
import com.q.rpc.CommonDecoder;
import com.q.rpc.CommonEncoder;
import com.q.rpc.RpcRegistry;
import com.q.rpc.impl.KryoSerializer;
import com.q.rpc.impl.ZkRegistry;
import com.q.utils.ReflectionUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Set;

@Slf4j
public class NettyTransportServerAbstract extends AbstractAutoTransportServer {
    private String host;
    private int port;
    private ServiceManager serviceManager;
    private RpcRegistry rpcRegistry;
    public NettyTransportServerAbstract(String host, int port){
        this.host=host;
        this.port=port;
        serviceManager=new DefaultServiceManager();
        this.rpcRegistry=new ZkRegistry();
        scanServices();
    }
    @Override
    public void start() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {

            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .option(ChannelOption.SO_BACKLOG, 256)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new CommonEncoder(new KryoSerializer()));
                            pipeline.addLast(new CommonDecoder());
                            pipeline.addLast(new NettyServerHandler());
                        }
                    });
            ChannelFuture future = serverBootstrap.bind(host,port).sync();
            //todo 好像不起作用
            ShutdownHook.getShutdownHook().addClearAllHook();
            future.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            log.error("启动服务器时有错误发生: ", e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
    @Override
    public <T> void publishService(Object service, Class<T> serviceClass) {
        serviceManager.register(service);
        rpcRegistry.register(serviceClass.getCanonicalName(), new InetSocketAddress(host, port));
    }

    @Override
    public void scanServices() {
        //通过调用栈获得启动类类名
        String mainClassName = ReflectionUtils.getStackTrace();
        Class<?> startClass;
        try {
            startClass = Class.forName(mainClassName);
            if(!startClass.isAnnotationPresent(ServiceScan.class)) {
                log.error("启动类缺少 @ServiceScan 注解");
                throw new RpcException(RpcErrorMessage.SERVICE_SCAN_PACKAGE_NOT_FOUND);
            }
        } catch (ClassNotFoundException e) {
            log.error("出现未知错误");
            throw new RpcException(RpcErrorMessage.UNKNOWN_ERROR);
        }
        String basePackage = startClass.getAnnotation(ServiceScan.class).value();
        if("".equals(basePackage)) {
            basePackage = mainClassName.substring(0, mainClassName.lastIndexOf("."));
        }
        Set<Class<?>> classSet = ReflectionUtils.getClasses(basePackage);
        for(Class<?> clazz : classSet) {
            if(clazz.isAnnotationPresent(Service.class)) {
                String serviceName = clazz.getAnnotation(Service.class).name();
                Object obj;
                try {
                    obj = clazz.newInstance();
                } catch (InstantiationException | IllegalAccessException e) {
                    log.error("创建 " + clazz + " 时有错误发生");
                    continue;
                }
                if("".equals(serviceName)) {
                    Class<?>[] interfaces = clazz.getInterfaces();
                    for (Class<?> oneInterface: interfaces){
                        publishService(obj, oneInterface);
                    }
                } else {
                    //todo 自定义服务名
                    Class<?>[] interfaces = clazz.getInterfaces();
                    for (Class<?> oneInterface: interfaces){
                        publishService(obj, oneInterface);
                    }
                }
            }
        }
    }

}
