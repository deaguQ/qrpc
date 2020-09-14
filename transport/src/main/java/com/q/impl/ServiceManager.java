package com.q.impl;
//用来管理本地服务
public interface ServiceManager {
    <T> void register(T service);
    Object getService(String serviceName);
}
