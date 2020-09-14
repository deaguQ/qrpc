package com.q.rpc;

import java.util.List;

public interface LoadBalance {
    String select(List<String> serviceUrlList);
}
