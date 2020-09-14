package com.q;

public interface TransportServer {
    void start();
    <T> void publishService(Object service, Class<T> serviceClass);
}
