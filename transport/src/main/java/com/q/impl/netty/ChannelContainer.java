package com.q.impl.netty;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
//保存Channel的容器
@Slf4j
public class ChannelContainer {
    private final Map<String, Channel> channelMap;
    public ChannelContainer() {
        channelMap = new ConcurrentHashMap<>();
    }
    public boolean containsChannel(String key) {
        return channelMap.containsKey(key);
    }

    public Channel getChannel(String key) {
        if(channelMap.containsKey(key)){
            return channelMap.get(key);
        }else{
            return null;
        }
    }

    public void removeChannel(String key) {
        channelMap.remove(key);
        log.info("Channel map size :[{}]", channelMap.size());
    }

    public void addChannel(String key, Channel channel) {
        channelMap.put(key, channel);
    }
}
