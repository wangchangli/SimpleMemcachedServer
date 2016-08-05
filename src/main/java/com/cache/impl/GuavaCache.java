package com.cache.impl;

import com.cache.Cache;
import com.cache.CacheException;
import com.cache.CacheValue;
import com.cache.Config;
import com.google.common.cache.CacheBuilder;

/**
 * Created by changliwang on 8/5/16.
 */
public class GuavaCache implements Cache{

    private com.google.common.cache.Cache<String, CacheValue> cache;

    public GuavaCache(){
        long cacheSize = Long.valueOf(Config.getProperty("cache.size", "100000000"));
        cache = CacheBuilder.newBuilder().maximumSize(cacheSize).build();
    }

    public void set(String key, CacheValue value, int expInSec) throws CacheException {
        // put a new instance every time, so it is thread safe
        cache.put(key, value);
    }

    public CacheValue get(String key) throws CacheException {
        CacheValue cacheValue = cache.getIfPresent(key);
        if(cacheValue != null && cacheValue.isExpired()){
            cache.invalidate(key);
            cacheValue = null;
        }
        return cacheValue;
    }

    public boolean delete(String key) throws CacheException {
        CacheValue cacheValue = cache.getIfPresent(key);
        cache.invalidate(key);
        return cacheValue != null;
    }
}
