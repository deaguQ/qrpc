package com.q.rpc.impl;

import com.q.rpc.LoadBalance;

import java.util.List;
import java.util.Random;

public class RandomLoadBalance implements LoadBalance {
    @Override
    public String select(List<String> serviceUrlList) {
        return serviceUrlList.get(new Random().nextInt(serviceUrlList.size()));
    }
}
