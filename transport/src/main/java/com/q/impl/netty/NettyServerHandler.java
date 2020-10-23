package com.q.impl.netty;

import com.q.Common;
import com.q.factory.SingletonFactory;
import com.q.message.RpcErrorMessage;
import com.q.proto.RpcRequest;
import com.q.proto.RpcResponse;
import com.q.utils.ProtoUtils;
import io.netty.channel.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class NettyServerHandler extends ChannelInboundHandlerAdapter {

    private static final RpcRequestHandler rpcRequestHandler=SingletonFactory.getInstance(RpcRequestHandler.class);
    private AtomicInteger timeoutCount = new AtomicInteger(0);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            log.info("服务器接收到请求: {}", msg);
            timeoutCount.set(0);
            RpcRequest rpcRequest = (RpcRequest) msg;
            //如果是心跳请求则对其响应心跳
            if (rpcRequest.getRpcMessageType() == Common.HEART_BEAT) {
                log.info("接收到客户端心跳请求");
                ctx.writeAndFlush(RpcResponse.builder().rpcMessageType(Common.HEART_BEAT).build());
            }else{
                Object result = rpcRequestHandler.handle(rpcRequest);
                log.info(String.format("server get result: %s", result.toString()));
                RpcResponse<Object> rpcResponse = ProtoUtils.successRpcResponse(result,rpcRequest.getRequestId());
                ctx.writeAndFlush(rpcResponse);
            }



/*            Object result = rpcRequestHandler.handle(rpcRequest);
            log.info(String.format("server get result: %s", result.toString()));
            if (ctx.channel().isActive() && ctx.channel().isWritable()) {
                RpcResponse<Object> rpcResponse = ProtoUtils.successRpcResponse(result,rpcRequest.getRequestId());
                ctx.writeAndFlush(rpcResponse).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            } else {
                RpcResponse<Object> rpcResponse = ProtoUtils.failRpcResponse(RpcErrorMessage.UNKNOWN_ERROR.getMessage());
                ctx.writeAndFlush(rpcResponse).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
                log.error("not writable now, message dropped");
            }*/


        } finally {
            ReferenceCountUtil.release(msg);
        }

    }

    private void handleRPCRequest(RpcRequest rpcRequest, ChannelHandlerContext ctx) {
        SingletonFactory.getThreadPool().submit(new Runnable() {
            @Override
            public void run() {
                Object result = rpcRequestHandler.handle(rpcRequest);
                log.info(String.format("server get result: %s", result.toString()));
                RpcResponse<Object> rpcResponse = ProtoUtils.successRpcResponse(result,rpcRequest.getRequestId());
                ctx.writeAndFlush(rpcResponse);
            }
        });
    }

    //心跳机制
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            //一段时间后客户端没有发送请求则关闭ctx
            if(((IdleStateEvent) evt).state().equals(IdleStateEvent.READER_IDLE_STATE_EVENT)){
                if (timeoutCount.getAndIncrement() >= Common.HEART_BEAT_TIME_OUT_MAX_TIME) {
                    ctx.close();
                    log.info("超过丢失心跳的次数阈值，关闭连接");
                }else {
                    log.info("超过规定时间服务器未收到客户端的心跳或正常信息");
                }
            }

        } else {
            super.userEventTriggered(ctx, evt);
        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("处理过程调用时有错误发生:");
        cause.printStackTrace();
        ctx.close();
    }
}
