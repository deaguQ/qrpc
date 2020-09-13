package com.q.service.impl;

import com.q.service.HelloService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HelloServiceImpl implements HelloService {
    @Override
    public String hello(String s) {
        log.info("接收到：{}",s);
        return s;
    }
}
