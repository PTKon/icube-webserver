package com.cn.ridge.baseInterface;

/**
 * 用于初始化es相关的配置
 * Author: create by wang.gf
 * Date: create at 2018/12/6
 */
public interface InitEs<T, R> {
    T getRestHighLevelClient();

    R getRequestOptions();
}
