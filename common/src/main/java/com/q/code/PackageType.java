package com.q.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * 协议包定义
 */
@AllArgsConstructor
@Getter
@ToString
public enum PackageType {
    REQUEST_PACK(0),
    RESPONSE_PACK(1);
    int code;
}
