package com.cn.ridge.es;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Author: create by wang.gf
 * Date: create at 2018/11/16
 */
@Component
@ConfigurationProperties
public class BaseDataEsInitBean {
    private static String hosts;
    private static Integer port;
    private static Integer bufferLimitBytes;
    private static String index;
    private static String type;

    @Value("${base.data.es.host}")
    public void setHosts(String hosts) {
        BaseDataEsInitBean.hosts = hosts;
    }

    @Value("${base.data.es.port}")
    public void setPort(Integer port) {
        BaseDataEsInitBean.port = port;
    }

    @Value("${base.data.es.bufferLimitBytes}")
    public void setBufferLimitBytes(Integer bufferLimitBytes) {
        BaseDataEsInitBean.bufferLimitBytes = bufferLimitBytes;
    }

    @Value("${base.data.es.index}")
    public void setIndex(String index) {
        BaseDataEsInitBean.index = index;
    }

    @Value("${base.data.es.doc.type}")
    public void setType(String type) {
        BaseDataEsInitBean.type = type;
    }

    static String getHosts() {
        return hosts;
    }

    static Integer getPort() {
        return port;
    }

    static Integer getBufferLimitBytes() {
        return bufferLimitBytes;
    }

    public static String getIndex() {
        return index;
    }

    public static String getType() {
        return type;
    }
}
