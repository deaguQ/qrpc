package com.q.impl.netty;

import com.q.Common;
import com.q.TransportClient;
import com.q.factory.SingletonFactory;
import com.q.proto.RpcRequest;
import com.q.proto.RpcResponse;
import com.q.rpc.RpcRegistry;
import com.q.rpc.impl.ZkRegistry;
import io.netty.channel.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class NettyClientHandler extends ChannelInboundHandlerAdapter {
    private final UnprocessedRequests unprocessedRequests;
    private final ChannelContainer channelContainer;
    private static NettyTransportClient nettyClient= SingletonFactory.getInstance(NettyTransportClient.class);
    private static final RpcRegistry rpcRegistry=SingletonFactory.getInstance(ZkRegistry.class);;
    private AtomicInteger timeoutCount = new AtomicInteger(0);

    public NettyClientHandler() {
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
        this.channelContainer=SingletonFactory.getInstance(ChannelContainer.class);
    }
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            log.info("client receive msg: [{}]", msg);
            timeoutCount.set(0);
            if (msg instanceof RpcResponse) {
                RpcResponse<Object> rpcResponse = (RpcResponse<Object>) msg;
                if (rpcResponse.getRpcMessageType() == Common.HEART_BEAT) {
                    log.info("接收到服务端心跳响应");
                }
                else{
                    unprocessedRequests.complete(rpcResponse);
                }

            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }
    //空闲事件触发回调
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            if (((IdleStateEvent) evt).state().equals(IdleStateEvent.WRITER_IDLE_STATE_EVENT)) {
                // 收不到服务端相应一段时间后则发送心跳,连续收不到则进行重连
                if (timeoutCount.getAndIncrement() >= Common.HEART_BEAT_TIME_OUT_MAX_TIME){
                    nettyClient.reConnect();
                }else{
                    log.info("write idle happen [{}]", ctx.channel().remoteAddress());
                    RpcRequest rpcRequest = RpcRequest.builder().rpcMessageType(Common.HEART_BEAT).build();
                    ctx.writeAndFlush(rpcRequest);
                }


            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("过程调用时有错误发生:");
        cause.printStackTrace();
        ctx.close();
    }
}
