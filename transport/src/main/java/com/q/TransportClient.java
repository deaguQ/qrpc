package com.q;

import com.q.proto.RpcRequest;
import com.q.proto.RpcResponse;
//客户端传输接口
public interface TransportClient {
    public RpcResponse sendRequest(RpcRequest request);
}
