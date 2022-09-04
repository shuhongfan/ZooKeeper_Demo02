package com.shf.zk;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.concurrent.TimeUnit;

public class Ticket12306 implements Runnable{
    private int tickets = 10;

    private InterProcessMutex lock;

    public Ticket12306() {
        ExponentialBackoffRetry retry = new ExponentialBackoffRetry(3000, 10);

        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString("192.168.120.20:2181") // 连接地址
                .sessionTimeoutMs(60 * 1000) // 会话超时时间
                .connectionTimeoutMs(15 * 1000) // 连接超时时间
                .retryPolicy(retry) // 重试策略
                .build();// 构建

        client.start();

        lock = new InterProcessMutex(client,"/lock");
    }

    @Override
    public void run() {
        while (true) {

//            获取锁
            try {
                lock.acquire(3, TimeUnit.SECONDS);

                if (tickets > 0) {
                    System.out.println(Thread.currentThread() + ":" + tickets);
                    tickets--;
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                //            释放锁
                try {
                    lock.release();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }




        }

    }
}
