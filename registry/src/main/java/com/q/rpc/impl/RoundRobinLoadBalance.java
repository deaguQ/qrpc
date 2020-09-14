package com.q.rpc.impl;

import com.q.rpc.LoadBalance;

import java.util.List;

public class RoundRobinLoadBalance implements LoadBalance {
    private int index=0;
    @Override
    public String select(List<String> serviceUrlList) {
         if(index==serviceUrlList.size())
             index=0;
         return serviceUrlList.get(index++);
    }
}
