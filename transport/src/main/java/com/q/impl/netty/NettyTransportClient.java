package com.q.impl.netty;

import com.q.TransportClient;
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
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

@Slf4j
public class NettyTransportClient implements TransportClient {

    private static final Bootstrap bootstrap;
    private static final RpcRegistry rpcRegistry;

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
                        pipeline.addLast(new CommonDecoder())
                                .addLast(new CommonEncoder(new KryoSerializer()))
                                .addLast(new NettyClientHandler());
                    }
                });
        rpcRegistry=new ZkRegistry();
    }


    @Override
    public RpcResponse sendRequest(RpcRequest request) {
        try {
            InetSocketAddress inetSocketAddress = rpcRegistry.lookupService(request.getInterfaceName());
            ChannelFuture future = bootstrap.connect(inetSocketAddress).sync();
            log.info("客户端连接到服务器 {}:{}", inetSocketAddress.getAddress(), inetSocketAddress.getPort());
            Channel channel = future.channel();
            if(channel != null) {
                channel.writeAndFlush(request).addListener(future1 -> {
                    if(future1.isSuccess()) {
                        log.info(String.format("客户端发送消息: %s", request.toString()));
                    } else {
                        log.error("发送消息时有错误发生: ", future1.cause());
                    }
                });
                //?
                channel.closeFuture().sync();
                //异步
                AttributeKey<RpcResponse> key = AttributeKey.valueOf("rpcResponse");
                RpcResponse rpcResponse = channel.attr(key).get();
                return rpcResponse;
            }
        } catch (InterruptedException e) {
            log.error("发送消息时有错误发生: ", e);
        }
        return null;
    }
}
