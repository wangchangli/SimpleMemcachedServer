package com.cache.protocol;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by changliwang on 8/5/16.
 */
public enum Cmd {
    set("set"),
    get("get"),
    delete("delete");

    private String name;
    Cmd(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static Cmd Of(String name) {
        for (Cmd cmd : Cmd.values()) {
            if (StringUtils.equals(cmd.getName(), name)) {
                return cmd;
            }
        }
        return null;
    }
}
