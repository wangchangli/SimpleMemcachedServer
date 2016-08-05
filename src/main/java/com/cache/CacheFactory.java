package com.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by changliwang on 8/5/16.
 */
public class CacheFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(CacheFactory.class);
    private static volatile Cache cache;

    public static Cache getCache() {
        if (null == cache) {
            synchronized (CacheFactory.class) {
                if (null == cache) {
                    String implClass = Config.getProperty("cache.impl.class", "com.cache.impl.GuavaCache");
                    try {
                        cache = (Cache) Class.forName(implClass).newInstance();
                    } catch (Exception e) {
                        LOGGER.error("Initialize cache failed.");
                        LOGGER.error(e.getMessage(), e);
                        System.exit(-1);
                    }
                }
            }
        }

        return cache;
    }

}
