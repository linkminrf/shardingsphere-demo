package com.lonk.util;


import java.util.UUID;

/**
 * @author lonk
 * @create 2021/2/1 16:44
 */
public class UUIDutil {
    /**
     * 生成id
     * @return
     */
    public static String getUUID(){
        return UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }
}
