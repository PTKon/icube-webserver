package com.cn.ridge.enums;

/**
 * Author: create by wang.gf
 * Date: create at 2018/10/29
 */
public enum QuartzKey {
    detail_name("-dn"),
    trigger_name("-tn"),
    group_name("-gn");

    private String key;

    QuartzKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
