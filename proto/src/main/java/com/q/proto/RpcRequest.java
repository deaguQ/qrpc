package com.q.proto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RpcRequest implements Serializable {
    /**
     * 待调用接口名称
     */
    private String interfaceName;
    /**
     * 待调用方法名称
     */
    private String methodName;
    /**
     * 调用方法的参数
     */
    private Object[] parameters;
    /**
     * 调用方法的参数类型
     */
    private Class<?>[] paramTypes;
    /**
     * 用来处理一个接口有多个实现类的情况
     */
    private String group;
    /**
     * 唯一标识一个request
     */
    private String requestId;
    /**
     * 用来标识是否是心跳 1为心跳
     */
    private int rpcMessageType;

    /**
     * 唯一确定该调用哪个服务
     * @return
     */
    public RpcServiceDescriptor toServiceDescriptor() {
        return RpcServiceDescriptor.builder().serviceName(this.getInterfaceName())
                .group(this.getGroup()).build();
    }
}
