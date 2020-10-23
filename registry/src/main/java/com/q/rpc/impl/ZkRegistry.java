package com.q.rpc.impl;

import com.q.exception.RpcException;
import com.q.message.RpcErrorMessage;
import com.q.proto.RpcServiceDescriptor;
import com.q.rpc.LoadBalance;
import com.q.rpc.RpcRegistry;
import com.q.utils.CuratorUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
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
    public InetSocketAddress lookupService(RpcServiceDescriptor rpcServiceDescriptor) {
        String serviceName=rpcServiceDescriptor.toRpcServiceName();
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
    //  一个类有多个接口，所有要对每个接口都注册一遍；而一个接口有多个实现类，所以要加上group确定唯一的实现类
    @Override
    public void publishService(RpcServiceDescriptor rpcServiceDescriptor,InetSocketAddress inetSocketAddress) {
        String servicePath = CuratorUtils.ZK_REGISTER_ROOT_PATH + "/" + rpcServiceDescriptor.toRpcServiceName() + inetSocketAddress.toString();
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        CuratorUtils.createPersistentNode(zkClient, servicePath);
    }
    //todo 服务端节点下线时回调方法,更新地址列表
    public void call(){

    }

}
