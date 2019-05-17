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
public class EsInitBean {
    private static String hosts;
    private static Integer port;
    private static Integer bufferLimitBytes;
    private static String index;
    private static String type;

    @Value("${capacity.es.host}")
    public void setHosts(String hosts) {
        EsInitBean.hosts = hosts;
    }

    @Value("${capacity.es.port}")
    public void setPort(Integer port) {
        EsInitBean.port = port;
    }

    @Value("${capacity.es.bufferLimitBytes}")
    public void setBufferLimitBytes(Integer bufferLimitBytes) {
        EsInitBean.bufferLimitBytes = bufferLimitBytes;
    }

    @Value("${capacity.es.index}")
    public void setIndex(String index) {
        EsInitBean.index = index;
    }

    @Value("${capacity.es.doc.type}")
    public void setType(String type) {
        EsInitBean.type = type;
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
