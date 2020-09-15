package com.q.impl.netty;

import com.q.TransportClient;
import com.q.factory.SingletonFactory;
import com.q.proto.RpcRequest;
import com.q.proto.RpcResponse;
import com.q.rpc.CommonDecoder;
import com.q.rpc.CommonEncoder;
import com.q.rpc.RpcRegistry;
import com.q.rpc.impl.JsonSerializer;
import com.q.rpc.impl.KryoSerializer;
import com.q.rpc.impl.ZkRegistry;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
public class NettyTransportClient implements TransportClient {

    private static final Bootstrap bootstrap;
    private static final RpcRegistry rpcRegistry;
    private static final UnprocessedRequests unprocessedRequests;
    private static final ChannelContainer channelContainer;
    static {
        EventLoopGroup group = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        //如果5秒内没有写数据，发送心跳
                        pipeline.addLast(new IdleStateHandler(0,5,0, TimeUnit.SECONDS))
                                .addLast(new CommonDecoder())
                                .addLast(new CommonEncoder(new KryoSerializer()))
                                .addLast(new NettyClientHandler());
                    }
                });

        rpcRegistry = SingletonFactory.getInstance(ZkRegistry.class);
        unprocessedRequests= SingletonFactory.getInstance(UnprocessedRequests.class);
        channelContainer=SingletonFactory.getInstance(ChannelContainer.class);
    }


    @Override
    public Object sendRequest(RpcRequest request) {
        CompletableFuture<RpcResponse<Object>> resultFuture = new CompletableFuture<>();
        InetSocketAddress inetSocketAddress = rpcRegistry.lookupService(request.toServiceDescriptor());
        Channel channel = doConnect(inetSocketAddress);
        if (channel != null&&channel.isActive()) {
            //存放未处理的请求
            unprocessedRequests.put(request.getRequestId(), resultFuture);
            channel.writeAndFlush(request).addListener((ChannelFutureListener) future1 -> {
                if (future1.isSuccess()) {
                    log.info(String.format("客户端发送消息: %s", request.toString()));
                } else {
                    //关闭连接
                    future1.channel().close();
                    resultFuture.completeExceptionally(future1.cause());
                    log.error("发送消息时有错误发生: ", future1.cause());
                }
            });
                /*//?
                channel.closeFuture().sync();
                //异步
                AttributeKey<RpcResponse> key = AttributeKey.valueOf("rpcResponse");
                RpcResponse rpcResponse = channel.attr(key).get();
                return rpcResponse;*/

        }else{
            throw new IllegalStateException();
        }

        return resultFuture;
    }

    //复用channel
    public static Channel doConnect(InetSocketAddress inetSocketAddress) {
        try {
            String key = inetSocketAddress.toString();
            // determine if there is a connection for the corresponding address
            if (channelContainer.containsChannel(key)) {
                Channel channel = channelContainer.getChannel(key);
                // if so, determine if the connection is available, and if so, get it directly
                if (channel != null && channel.isActive()) {
                    return channel;
                } else {
                    channelContainer.removeChannel(key);
                }
            }
            ChannelFuture future = bootstrap.connect(inetSocketAddress).sync();
            log.info("客户端连接到服务器 {}:{}", inetSocketAddress.getAddress(), inetSocketAddress.getPort());
            Channel channel = future.channel();
            channelContainer.addChannel(key, channel);
            return channel;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
}
