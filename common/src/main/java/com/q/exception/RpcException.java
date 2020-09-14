package com.q.exception;

import com.q.message.RpcErrorMessage;
//同一异常处理
public class RpcException extends RuntimeException{
    public RpcException(RpcErrorMessage rpcErrorMessage, String detail) {
        super(rpcErrorMessage.getMessage() + ":" + detail);
    }
    public RpcException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcException(RpcErrorMessage rpcErrorMessage) {
        super(rpcErrorMessage.getMessage());
    }
}
