package com.q.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public enum SerializerCode {
    KRYO(0),
    JSON(1);
    int code;
}
