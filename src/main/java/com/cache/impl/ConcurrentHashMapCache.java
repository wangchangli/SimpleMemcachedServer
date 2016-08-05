package com.cache.impl;

import com.cache.Cache;
import com.cache.CacheValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by changliwang on 8/2/16.
 *
 * The cache values are soft reference objects, they will be cleared at the discretion of the garbage
 * collector in response to memory demand.
 */
public class ConcurrentHashMapCache implements Cache {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConcurrentHashMapCache.class);

    private final Map<String, SoftCacheValue> cache = new ConcurrentHashMap();

    private final ReferenceQueue referenceQueue = new ReferenceQueue();

    private long cleanInterval = 120 * 1000; // 2min

    private Thread cleanThread = null;

    public ConcurrentHashMapCache() {
        startCleanThread();
    }


    private static class SoftCacheValue extends SoftReference {
        private final Object key; // we need the key when remove the garbage from the map

        private SoftCacheValue(Object value, Object key, ReferenceQueue referenceQueue) {
            super(value, referenceQueue);
            this.key = key;
        }
    }

    private void startCleanThread() {
        cleanThread = new Thread("Clean Thread") {
            public void run() {
                long timeToSleep;
                long startTime;
                long endTime;
                long cleanTime = 0;
                long expCount;
                long garbageCount;
                while (true) {
                    timeToSleep = cleanInterval - cleanTime;
                    if (timeToSleep > 0) {
                        try {
                            Thread.sleep(timeToSleep);
                        } catch (InterruptedException ex) {
                            LOGGER.warn("The clean thread was interrupted.");
                            break;
                        }
                    }

                    LOGGER.info("Begin a clean process...");

                    startTime = System.currentTimeMillis();

                    long cacheSize = cache.size();

                    expCount = cleanExpiredValue();

                    garbageCount = cleanGarbageValue();

                    endTime = System.currentTimeMillis();

                    cleanTime = endTime - startTime;

                    LOGGER.info("Clean result:");
                    LOGGER.info("===================");
                    LOGGER.info("Scan: {}", cacheSize);
                    LOGGER.info("Time: {}ms", cleanTime);
                    LOGGER.info("Expired: {}", expCount);
                    LOGGER.info("GC: {}", garbageCount);
                    LOGGER.info("===================");
                }
            }
        };

        cleanThread.setDaemon(true);
        cleanThread.start();
    }

    public long cleanExpiredValue() {
        long expCount = 0;
        try {
            Iterator<String> iterator = cache.keySet().iterator();
            String key;
            CacheValue cacheValue;
            while (iterator.hasNext()) {
                key = iterator.next();
                cacheValue = (CacheValue) cache.get(key).get();
                if(cacheValue != null) { // not collected by gc
                    if (cacheValue.isExpired()) {
                        expCount++;
                        cache.remove(key);
                        LOGGER.debug("Clean expired key:" + key);
                    }
                }else{
                    cache.remove(key);
                }
                Thread.yield();
            }
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        return expCount;
    }

    private long cleanGarbageValue() {
        int garbageCount = 0;
        SoftCacheValue softCacheValue;
        try {
            while ((softCacheValue = (SoftCacheValue) referenceQueue.poll()) != null) {
                garbageCount++;
                cache.remove(softCacheValue.key);
                LOGGER.debug("clean garbage key " + softCacheValue.key);

                Thread.yield();
            }
        } catch (Throwable ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        return garbageCount;
    }

    public void set(String key, CacheValue value, int expTimeInSec) {
        // put a new instance every time, so it is thread safe
        cache.put(key, new SoftCacheValue(value, key, referenceQueue));
    }


    public CacheValue get(String key) {
        CacheValue cacheValue = null;
        SoftReference softReference = cache.get(key);
        if (softReference != null) { // key exist
            cacheValue = (CacheValue)softReference.get();
            if (cacheValue == null) { // collected by gc already, remove the garbage on the map
                cache.remove(key);
            } else {
                if (cacheValue.isExpired()) { // remove the the cache if expire
                    cache.remove(key);
                    cacheValue = null;
                }
            }
        }
        return cacheValue;
    }

    public boolean delete(String key) {
        Object object = cache.remove(key);
        return object != null;
    }

}
