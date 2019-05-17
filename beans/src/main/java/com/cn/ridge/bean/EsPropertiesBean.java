package com.cn.ridge.bean;

import com.cn.ridge.baseInterface.InitEs;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;

/**
 * Author: create by wang.gf
 * Date: create at 2018/12/26
 */
public class EsPropertiesBean {
    private String index;
    private String docType;
    private InitEs<RestHighLevelClient, RequestOptions> esClient;

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public InitEs<RestHighLevelClient, RequestOptions> getEsClient() {
        return esClient;
    }

    public void setEsClient(InitEs<RestHighLevelClient, RequestOptions> esClient) {
        this.esClient = esClient;
    }

    public EsPropertiesBean(String index, String docType, InitEs<RestHighLevelClient, RequestOptions> esClient) {
        this.index = index;
        this.docType = docType;
        this.esClient = esClient;
    }
}
