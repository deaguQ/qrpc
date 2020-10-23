package com.q.impl.netty;

import com.q.ShutdownHook;
import com.q.exception.RpcException;
import com.q.factory.SingletonFactory;
import com.q.AbstractAutoTransportServer;
import com.q.ServiceManager;
import com.q.impl.annotations.Service;
import com.q.impl.annotations.ServiceScan;
import com.q.message.RpcErrorMessage;
import com.q.proto.RpcServiceDescriptor;
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
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
public class NettyTransportServer extends AbstractAutoTransportServer {
    private String host;
    private int port;
    private ServiceManager serviceManager;
    private RpcRegistry rpcRegistry;
    public NettyTransportServer(String host, int port){
        this.host=host;
        this.port=port;
        serviceManager= SingletonFactory.getInstance(DefaultServiceManager.class);
        this.rpcRegistry=SingletonFactory.getInstance(ZkRegistry.class);
        scanServices();
    }
    @Override
    public void start() {
        //服务端正常关闭时注销所有注册服务
        ShutdownHook.getShutdownHook().addClearAllHook();
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {

            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .option(ChannelOption.SO_BACKLOG, 256)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    // TCP默认开启了 Nagle 算法，该算法的作用是尽可能的发送大数据快，减少网络传输。TCP_NODELAY 参数的作用就是控制是否启用 Nagle 算法。
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    // 是否开启 TCP 底层心跳机制
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            //心跳功能，30 秒之内没有收到客户端请求的话就关闭连接
                            pipeline.addLast(new IdleStateHandler(120, 120, 120, TimeUnit.SECONDS));
                            pipeline.addLast(new CommonEncoder(new KryoSerializer()));
                            pipeline.addLast(new CommonDecoder());
                            pipeline.addLast(new NettyServerHandler());
                        }
                    });
            ChannelFuture future = serverBootstrap.bind(host,port).sync();
            future.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            log.error("启动服务器时有错误发生: ", e);
        } finally {
            log.error("shutdown bossGroup and workerGroup");
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
    @Override
    public <T> void publishService(Object service, RpcServiceDescriptor serviceDescriptor) {
        serviceManager.register(service,serviceDescriptor);
        rpcRegistry.publishService(serviceDescriptor, new InetSocketAddress(host, port));
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
                String group = clazz.getAnnotation(Service.class).group();
                Object obj;
                try {
                    obj = clazz.newInstance();
                } catch (InstantiationException | IllegalAccessException e) {
                    log.error("创建 " + clazz + " 时有错误发生");
                    continue;
                }
                //增加group属性标识同一接口不同的实现类
                if("".equals(group)) {
                    Class<?>[] interfaces = clazz.getInterfaces();

                    for (Class<?> oneInterface: interfaces){
                        publishService(obj, RpcServiceDescriptor.builder().serviceName(oneInterface.getCanonicalName()).group("").build());
                    }
                } else {
                    //
                    Class<?>[] interfaces = clazz.getInterfaces();
                    for (Class<?> oneInterface: interfaces){
                        publishService(obj, RpcServiceDescriptor.builder().serviceName(oneInterface.getCanonicalName()).group(group).build());
                    }
                }
            }
        }
    }

}
