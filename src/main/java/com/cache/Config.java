package com.cache;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by changliwang on 8/5/16.
 */
public class Config {
    private static Logger LOGGER = LoggerFactory.getLogger(Config.class);
    private static final Properties PROPERTIES = new Properties();

    static {
        InputStream input = Config.class.getClassLoader().getResourceAsStream("cache.properties");
        try {
            PROPERTIES.load(input);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
    }

    public static String getProperty(String key, String defaultValue) {
        String value = PROPERTIES.getProperty(key);
        if(StringUtils.isBlank(value)){
            return defaultValue;
        }
        return value;
    }
}
