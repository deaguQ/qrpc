package com.q.utils;

import com.q.proto.RpcResponse;

public class ProtoUtils {
    public static int successCode=200;
    public static int failCode=500;

    /**
     * 获得成功相应
     * @param data
     * @param <T>
     * @return
     */
    public static <T> RpcResponse<T> successRpcResponse(T data){
        RpcResponse<T> response = new RpcResponse<>();
        response.setStatusCode(successCode);
        response.setData(data);
        return response;
    }
    public static <T> RpcResponse<T> successRpcResponse(T data,String requestId){
        RpcResponse<T> response = new RpcResponse<>();
        response.setStatusCode(successCode);
        if(null!=data){
            response.setData(data);
        }
        response.setRequestId(requestId);
        return response;
    }
    /**
     * 获得失败响应
     * @param msg 提示信息
     * @param <T>
     * @return
     */
    public static <T> RpcResponse<T> failRpcResponse(String msg) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setStatusCode(failCode);
        response.setMessage(msg);
        return response;
    }
}
