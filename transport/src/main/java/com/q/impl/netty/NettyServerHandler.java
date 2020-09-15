package com.q.impl.netty;

import com.q.Common;
import com.q.RequestHandler;
import com.q.factory.SingletonFactory;
import com.q.impl.DefaultServiceManager;
import com.q.impl.ServiceManager;
import com.q.message.RpcErrorMessage;
import com.q.proto.RpcRequest;
import com.q.proto.RpcResponse;
import com.q.rpc.RpcRegistry;
import com.q.utils.ProtoUtils;
import io.netty.channel.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.spi.ServiceRegistry;

@Slf4j
public class NettyServerHandler extends ChannelInboundHandlerAdapter {

    private static final RpcRequestHandler rpcRequestHandler;

    static {
        rpcRequestHandler = SingletonFactory.getInstance(RpcRequestHandler.class);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            log.info("服务器接收到请求: {}", msg);
            RpcRequest rpcRequest = (RpcRequest) msg;
            //如果是心跳请求不做处理
            if (rpcRequest.getRpcMessageType() == Common.HEART_BEAT) {
                log.info("接收到心跳请求");
                return;
            }
            Object result = rpcRequestHandler.handle(rpcRequest);
            log.info(String.format("server get result: %s", result.toString()));
            if (ctx.channel().isActive() && ctx.channel().isWritable()) {
                RpcResponse<Object> rpcResponse = ProtoUtils.successRpcResponse(result,rpcRequest.getRequestId());
                ctx.writeAndFlush(rpcResponse).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            } else {
                RpcResponse<Object> rpcResponse = ProtoUtils.failRpcResponse(RpcErrorMessage.UNKNOWN_ERROR.getMessage());
                ctx.writeAndFlush(rpcResponse).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
                log.error("not writable now, message dropped");
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }

    }

    //心跳机制
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            //没有客户端发送请求则关闭ctx
            if (state == IdleState.READER_IDLE) {
                log.info("idle check happen, so close the connection");
                ctx.close();
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
