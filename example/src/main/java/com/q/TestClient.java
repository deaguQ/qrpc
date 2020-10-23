package com.q;

import com.q.domain.User;
import com.q.service.HelloService;
import com.q.service.UserService;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
@Slf4j
public class TestClient {
    static int warmupIterations=5;
    static int measurementIterations=10;
    static int threads=50;
    static int requestsPerThread=20;
    static int requestsTotal=threads*requestsPerThread;
    static ExecutorService executorService= Executors.newFixedThreadPool(threads);
    public static void main(String[] args) {
        RpcClient client = new RpcClient();
        //todo 消费端注解自动注入
        //true时开启netty传输false时使用socket
        HelloService service = client.getProxy(HelloService.class,true);
        System.out.println(service.hello("hello server"));
        HelloService service2 = client.getProxy(HelloService.class,true,"impl2");
        System.out.println(service2.hello("hello server"));

        UserService userService=client.getProxy(UserService.class,true);
        List<double[]> res=new ArrayList<>();
        try {
            for (int i = 0; i < warmupIterations + measurementIterations; i++) {
                CountDownLatch countDownLatch = new CountDownLatch(threads);

                User user = new User();
                long id = 1L;
                user.setId(id);
                user.setName("qiu" + id);
                user.setSex(1);
                user.setBirthday(LocalDate.of(1968, 12, 8));
                user.setEmail("qiu@gmail.com" + id);
                user.setMobile("110" + id);
                user.setAddress("北京交通大学" + id);
                user.setIcon("https://www.baidu.com/img/1" + id);
                user.setStatus(1);
                user.setCreateTime(LocalDateTime.now());
                user.setUpdateTime(user.getCreateTime());

                List<Integer> permissions = new ArrayList<Integer>(
                        Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 19, 88, 86, 89, 90, 91, 92));
                user.setPermissions(permissions);

                List<Long> rts = new Vector<>(3000);
                Runnable r = () -> {
                    for (int j = 0; j < requestsPerThread; j++) {
                        long begin = System.nanoTime();
                        try {
                            userService.createUser(user);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        rts.add(System.nanoTime() - begin);
                    }
                    countDownLatch.countDown();
                };


                for (int k = 0; k < threads; k++) {
                    executorService.submit(r);
                }
                long benchmarkStart = System.nanoTime();
                countDownLatch.await();
                long nanos = System.nanoTime() - benchmarkStart;
                if (i >= warmupIterations) {
                    res.add(benchmarkResult(i - warmupIterations,nanos,rts));
                }
            }
            avg(res);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }



    }

    private static void avg(List<double[]> res) {
        double qps=0;
        double avgRt=0;
        for(double[] i:res){
            qps+=i[0];
            avgRt+=i[1];
        }
        qps=qps/res.size();
        avgRt=avgRt/res.size();
        log.info("qps:{} avgRt:{}",qps,avgRt);
        System.out.println(qps+"----"+avgRt);
    }

    public static double[] benchmarkResult(int index, long nanos, List<Long> rts){
        // 每毫秒的处理请求数
        double qps = 1.0 * requestsTotal * 1000000 / nanos;
        // 平均响应时间 毫秒
        double avgRt = 1.0 * rts.stream().mapToLong(x -> x).sum() / 1000000 / requestsTotal;
        log.info("qps:{} rt:{}",qps,avgRt);

        return new double[]{qps,avgRt};
    }

}
