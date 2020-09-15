package com.q.proto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class RpcServiceDescriptor {
    /**
     * when the interface has multiple implementation classes, distinguish by group
     */
    private String group;
    private String serviceName;
    public String toRpcServiceName() {
        return this.getServiceName() + this.getGroup();
    }

}
