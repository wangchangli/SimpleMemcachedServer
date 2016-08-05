package com.cache;

import net.spy.memcached.AddrUtil;
import net.spy.memcached.MemcachedClient;
import org.junit.Assert;

import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;

/**
 * Created by changliwang on 8/2/16.
 */
public class AccuracyTest {

    private static class Person implements Serializable{
        private String name;
        private int age;
        Person(String name, int age){
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }
    }

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        // please launch the cache server first
        final MemcachedClient memcachedClient = new MemcachedClient(AddrUtil.getAddresses("127.0.0.1:11211"));

        // string
        Assert.assertTrue(memcachedClient.set("key1", 0, "value1").get());
        Assert.assertEquals("value1", memcachedClient.get("key1"));
        Assert.assertTrue(memcachedClient.delete("key1").get());
        Assert.assertNull(memcachedClient.get("key1"));

        memcachedClient.set("key2", 0, "valu\r\ne2").get();
        Assert.assertEquals("valu\r\ne2", memcachedClient.get("key2"));

        // POJO
        memcachedClient.set("key3", 0, new Person("wangchangli", 18));
        Person person = (Person) memcachedClient.get("key3");
        Assert.assertEquals("wangchangli", person.getName());
        Assert.assertEquals(18, person.getAge());

        final CyclicBarrier cyclicBarrier = new CyclicBarrier(5);
        for(int i=0; i<5; i++) {
            final int index = i;
            new Thread(new Runnable() {
                public void run() {
                    try {
                        cyclicBarrier.await();

                        for(int j=0; j<10000; j++) {
                            String key = "key_" + index+"_"+j;
                            String value = "value_" + index+"_"+j;
                            Assert.assertTrue(memcachedClient.set(key, 0, value).get());
                            Assert.assertEquals(value, memcachedClient.get(key));
                            Assert.assertTrue(memcachedClient.delete(key).get());
                            Assert.assertNull(memcachedClient.get(key));
                        }

                    } catch (InterruptedException e) {
                        Assert.fail();
                    } catch (BrokenBarrierException e) {
                        Assert.fail();
                    } catch (ExecutionException e) {
                        Assert.fail();
                    }
                }
            }).start();
        }

        System.out.println("Test finished.");
    }
}
