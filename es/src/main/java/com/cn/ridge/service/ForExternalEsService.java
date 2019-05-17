package com.cn.ridge.service;

import com.alibaba.fastjson.JSONObject;
import com.cn.ridge.bean.EsPropertiesBean;
import com.cn.ridge.bean.ForExternalPageBean;
import com.cn.ridge.enums.EsSetting;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Author: create by wang.gf
 * Date: create at 2018/12/28
 */
@Service("forExternalEsService")
public class ForExternalEsService {

    private static final Logger logger = LogManager.getLogger(ForExternalEsService.class);

    /**
     * 为外部项目访问容量，获得数据提供接口
     *
     * @param pageBean 分页对象
     * @param client   es客户端
     * @return json对象
     * @throws Exception 异常抛出
     */
    public JSONObject getCapacitySource(ForExternalPageBean pageBean, EsPropertiesBean client) throws Exception {
        //字段过滤
        pageBean.setIncludeField(EsSetting.CAPACITY_VIC_NEED_FIELD.getKey());
        //非空判断
        Integer from = pageBean.getFrom();
        Integer pageSize = pageBean.getPageSize();
        Assert.isTrue(from != null && pageSize != null && pageSize != 0, "from不能是null，pageSize不能是null，pageSize不能是0！！！");
        String orderField = pageBean.getOrderField();
        Assert.hasText(orderField, "orderField不能是null/''！！！！");
        Integer orderType = pageBean.getOrderType();
        Assert.isTrue(orderType != null && (orderType == 0 || orderType == 1), "orderType不能是null，并且只能是0或者1！！");
        Long ciId = pageBean.getCiId();
        Long kpiCode = pageBean.getKpiCode();
        Assert.notNull(ciId, "ciId不能是null！！");
        Assert.notNull(kpiCode, "kpiCode不能是null！！");
        //client
        RestHighLevelClient highLevelClient = client.getEsClient().getRestHighLevelClient();
        //search request
        SearchRequest searchRequest = new SearchRequest(client.getIndex());
        //doc type
        searchRequest.types(client.getDocType()).scroll(new TimeValue(1, TimeUnit.MINUTES));
        //bool query builder
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        //未删除
        queryBuilder
                //置信区间上界线数据
                .must(QueryBuilders.termsQuery("type", EsSetting.CAPACITY_UPPER_BOUNDARY_LINE.getKey(),
                        EsSetting.CAPACITY_LOWER_BOUNDARY_LINE.getKey(), EsSetting.CAPACITY_RESULT.getKey()))
                //ci id
                .must(QueryBuilders.matchQuery("ciId", ciId))
                //kpi code
                .must(QueryBuilders.matchQuery("kpiId", kpiCode));
        //instance
        if (StringUtils.isNotBlank(pageBean.getInstance()))
            queryBuilder.must(QueryBuilders.matchQuery("instanceId", pageBean.getInstance()));
        //data start and end
        if (StringUtils.isNotBlank(pageBean.getStartAt()) && StringUtils.isBlank(pageBean.getEndAt())) {
            queryBuilder.filter(QueryBuilders.rangeQuery("forecastDateTime").gte(pageBean.getStartAt()));
        } else if (StringUtils.isBlank(pageBean.getStartAt()) && StringUtils.isNotBlank(pageBean.getEndAt())) {
            queryBuilder.filter(QueryBuilders.rangeQuery("forecastDateTime").lte(pageBean.getEndAt()));
        } else if (StringUtils.isNotBlank(pageBean.getStartAt()) && StringUtils.isNotBlank(pageBean.getEndAt())) {
            queryBuilder.filter(QueryBuilders.rangeQuery("forecastDateTime")
                    .gte(pageBean.getStartAt())
                    .lte(pageBean.getEndAt()));
        }
        //source builder
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(queryBuilder);
        sourceBuilder.from(from);
        sourceBuilder.size(pageSize);
        //要求返回固定对象中的某些字段
        if (StringUtils.isNotBlank(pageBean.getIncludeField())) {
            sourceBuilder.fetchSource(pageBean.getIncludeField().split(","), Strings.EMPTY_ARRAY);
        }
        //order
        if (0 == orderType)
            sourceBuilder.sort(new FieldSortBuilder(orderField).order(SortOrder.ASC));
        else
            sourceBuilder.sort(new FieldSortBuilder(orderField).order(SortOrder.DESC));
        sourceBuilder.timeout(new TimeValue(20, TimeUnit.SECONDS));
        //add source
        searchRequest.source(sourceBuilder);
        SearchResponse searchResponse = highLevelClient.search(searchRequest, client.getEsClient().getRequestOptions());
        if (RestStatus.OK == searchResponse.status()) {
            if (0 == searchResponse.getFailedShards()) {
                if (0L == searchResponse.getHits().totalHits) {
                    return null;
                } else {
                    return JSONObject.parseObject(searchResponse.toString());
                }
            } else {
                Arrays.stream(searchResponse.getShardFailures()).forEach(s ->
                        logger.error("检索任务名称是否存在发生异常是：" + s.reason()));
                throw new RuntimeException("分页获得对象发生异常！！");
            }
        } else {
            logger.error("查询发生异常！！！错误code是：" + searchResponse.status());
            throw new RuntimeException("查询发生异常，异常code是：" + searchResponse.status());
        }
    }
}
