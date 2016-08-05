package com.cache;

/**
 * Created by changliwang on 8/2/16.
 */
public interface Cache {
    void set(String key, CacheValue value, int expInSec) throws CacheException;
    CacheValue get(String key) throws CacheException;
    boolean delete(String key) throws CacheException;
}
