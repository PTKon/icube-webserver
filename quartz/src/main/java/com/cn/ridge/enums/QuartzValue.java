package com.cn.ridge.enums;

/**
 * Author: create by wang.gf
 * Date: create at 2019/1/2
 */
public enum QuartzValue {
    last_day(99),
    day_is_null(88);//如果没有设定day字段

    private Integer value;

    QuartzValue(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }
}
