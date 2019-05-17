package com.cn.ridge.enums;

/**
 * Author: create by wang.gf
 * Date: create at 2018/11/22
 */
public enum ExcelSetting {
    max_count(1000);

    private Integer value;

    ExcelSetting(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }
}
