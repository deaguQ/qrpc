package com.q.rpc.impl;

import com.q.exception.RpcException;
import com.q.message.RpcErrorMessage;
import com.q.rpc.LoadBalance;
import com.q.rpc.RpcRegistry;
import com.q.utils.CuratorUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;

import java.net.InetSocketAddress;
import java.util.List;
@Slf4j
public class ZkRegistry implements RpcRegistry {
    private LoadBalance loadBalance;
    public ZkRegistry(){
        this.loadBalance=new RandomLoadBalance();
    }
    public ZkRegistry(LoadBalance loadBalance){
        this.loadBalance=loadBalance;
    }
    @Override
    public void register(String serviceName, InetSocketAddress inetSocketAddress) {
        String servicePath = CuratorUtils.ZK_REGISTER_ROOT_PATH + "/" + serviceName + inetSocketAddress.toString();
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        CuratorUtils.createPersistentNode(zkClient, servicePath);
    }

    @Override
    public InetSocketAddress lookupService(String serviceName) {
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        List<String> serviceUrlList = CuratorUtils.getChildrenNodes(zkClient, serviceName);
        if (serviceUrlList.size() == 0) {
            throw new RpcException(RpcErrorMessage.SERVICE_CAN_NOT_BE_FOUND, serviceName);
        }
        String targetServiceUrl = loadBalance.select(serviceUrlList);
        log.info("Successfully found the service address:[{}]", targetServiceUrl);
        String[] socketAddressArray = targetServiceUrl.split(":");
        String host = socketAddressArray[0];
        int port = Integer.parseInt(socketAddressArray[1]);
        return new InetSocketAddress(host, port);
    }
}
