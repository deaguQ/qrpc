package com.q.message;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public enum RpcErrorMessage {
    CLIENT_CONNECT_SERVER_FAILURE("客户端连接服务端失败"),
    SERVICE_INVOCATION_FAILURE("服务调用失败"),
    SERVICE_CAN_NOT_BE_FOUND("没有找到指定的服务"),
    SERVICE_NOT_IMPLEMENT_ANY_INTERFACE("注册的服务没有实现任何接口"),
    REQUEST_NOT_MATCH_RESPONSE("返回结果错误！请求和返回的相应不匹配"),
    METHOD_NOT_FOUND("没有找到适用方法"), UNKNOWN_PROTOCOL("非法协议"), UNKNOWN_PACKAGE_TYPE("未识别的数据包"), UNKNOWN_SERIALIZER("未识别的序列号算法"),
    SERIALIZE_FAIL("序列化失败"), SERVICE_SCAN_PACKAGE_NOT_FOUND("启动类缺少 @ServiceScan 注解"), UNKNOWN_ERROR("未知错误");
    private final String message;

}
