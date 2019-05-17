package com.cn.ridge.es;

import com.cn.ridge.baseInterface.InitEs;
import org.apache.http.HttpHost;
import org.elasticsearch.client.HttpAsyncResponseConsumerFactory;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Author: create by wang.gf
 * Date: create at 2018/12/7
 */
public enum EsClient implements InitEs<RestHighLevelClient, RequestOptions> {
    client;

    private RestHighLevelClient restHighLevelClient;
    private RequestOptions requestOptions;

    EsClient() {
        /*rest high level client*/
        restHighLevelClient = new RestHighLevelClient(
                RestClient.builder(Arrays.stream(EsInitBean.getHosts().split(","))
                        .map(host -> new HttpHost(host, EsInitBean.getPort(), "http"))
                        .collect(Collectors.toList())
                        .toArray(new HttpHost[]{})));

        /*request options*/
        //请求结果最大buffer
        HttpAsyncResponseConsumerFactory.HeapBufferedResponseConsumerFactory factory =
                new HttpAsyncResponseConsumerFactory.HeapBufferedResponseConsumerFactory(
                        EsInitBean.getBufferLimitBytes());
        //获得builder
        RequestOptions.Builder builder = RequestOptions.DEFAULT.toBuilder();
        //请求头
        builder.addHeader("Content-Type", "application/json;charset=utf-8");
        //set 工厂
        builder.setHttpAsyncResponseConsumerFactory(factory);
        requestOptions = builder.build();
    }

    public RestHighLevelClient getRestHighLevelClient() {
        return restHighLevelClient;
    }

    public RequestOptions getRequestOptions() {
        return requestOptions;
    }
}
