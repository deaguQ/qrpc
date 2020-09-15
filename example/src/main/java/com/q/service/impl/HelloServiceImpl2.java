package com.q.service.impl;

import com.q.impl.annotations.Service;
import com.q.service.HelloService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service(group = "impl2")
public class HelloServiceImpl2 implements HelloService {
    @Override
    public String hello(String s) {
        log.info("HelloServiceImpl2接收到：{}",s);
        return "HelloServiceImpl2接收到"+s;
    }
}