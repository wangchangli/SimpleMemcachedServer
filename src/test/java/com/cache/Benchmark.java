package com.cache;

import net.spy.memcached.AddrUtil;
import net.spy.memcached.ConnectionFactory;
import net.spy.memcached.ConnectionFactoryBuilder;
import net.spy.memcached.MemcachedClient;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by changliwang on 8/4/16.
 */
public class Benchmark {

    private static AtomicInteger failedRequests = new AtomicInteger();

    public static void main(String args[]) throws IOException {
        String host = "127.0.0.1:11211";
        int threads = 8;
        int requests = 100000;

        if (args.length == 3) {
            host = args[0];
            threads = Integer.parseInt(args[1]);
            requests = Integer.parseInt(args[2]);
        }

        int eachThreadRequests = requests / threads;

        Thread threadPool[] = new Thread[threads];
        for (int i = 0; i < threads; i++) {
            threadPool[i] = new Thread(new LoadTest("Test-" + i, host, eachThreadRequests));
        }

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < threads; i++) {
            threadPool[i].start();
        }

        for (int i = 0; i < threads; i++) {
            try {
                threadPool[i].join();
            } catch (InterruptedException ex) {
                System.out.println("Thread was interrupted!");
            }
        }
        long timeCost = System.currentTimeMillis() - startTime;

        long qps = (requests-failedRequests.get()) * 1000 / timeCost;

        System.out.println("Benchmark result:");
        System.out.println("=============");
        System.out.println("Total Threads: " + threads);
        System.out.println("Total Requests: " + requests);
        System.out.println("Total Failed Requests: " + failedRequests.get());
        System.out.println("Total Time: " + timeCost + " ms");
        System.out.println("QPS: " + qps);
        System.out.println("=============");

        try {
            Thread.sleep(5000);
        } catch (InterruptedException ex) {
            System.out.println();
        }
    }

    private static class LoadTest implements Runnable {
        private String name;
        private String host;
        private int timeouts;
        private int requests;
        private static MemcachedClient client = null;
        private static int objectSize = 1024;//1kb
        private AtomicInteger failRequests;

        LoadTest(String name, String host, int requests) throws IOException {
            this.name = name;
            this.host = host;
            this.requests = requests;
            this.failRequests = new AtomicInteger();
            ConnectionFactory connectionFactory = new ConnectionFactoryBuilder().setOpTimeout(1000).build(); //1s timeout
            client = new MemcachedClient(connectionFactory, AddrUtil.getAddresses(host));
        }

        public void run() {
            StringBuilder sb = new StringBuilder();
            Random r = new Random();
            char c = '0';
            for (int k = 0; k < 20; k++) {
                c = (char) (r.nextInt(26) + 'a');
                sb.append(c);
            }
            String seed = sb.toString();

            requests = requests / 3;

            for (int i = 0; i < requests; i++) {
                doSet(seed, i);
            }
            for (int i = 0; i < requests; i++) {
                doGet(seed, i);
            }
            for (int i = 0; i < requests; i++) {
                doDelete(seed, i);
            }

            Benchmark.failedRequests.addAndGet(failRequests.get());
        }

        private String largeData = null;

        public void doSet(String seed, int i) {
            String key = name + "-" + i + "-" + seed;
            if (largeData == null) {
                StringBuilder sb = new StringBuilder();
                Random r = new Random();
                char c = '0';
                for (int k = 0; k < objectSize; k++) {
                    c = (char) (r.nextInt(26) + 'a');
                    sb.append(c);
                }
                largeData = sb.toString();
            }
            String value = name + "-" + (i * 2) + "-" + largeData;
            try {
                client.set(key, 3600, value).get();
            } catch (InterruptedException e) {
                failRequests.incrementAndGet();
            } catch (ExecutionException e) {
                failRequests.incrementAndGet();
            }
        }

        public void doGet(String seed, int i) {
            String key = name + "-" + i + "-" + seed;
            Object value = client.get(key);
            if (value == null) {
                failRequests.incrementAndGet();
            }
        }

        public void doDelete(String seed, int i) {
            String key = name + "-" + i + "-" + seed;
            try {
                client.delete(key).get();
            } catch (InterruptedException e) {
                failRequests.incrementAndGet();
            } catch (ExecutionException e) {
                failRequests.incrementAndGet();
            }
        }
    }

}
