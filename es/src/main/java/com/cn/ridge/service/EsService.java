package com.cn.ridge.service;

import com.alibaba.fastjson.JSONObject;
import com.cn.ridge.bean.*;
import com.cn.ridge.enums.EsSetting;
import com.cn.ridge.enums.ExcelSetting;
import com.cn.ridge.util.LocalDateTimeUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.ElasticsearchCorruptionException;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Author: create by wang.gf
 * Date: create at 2018/11/15
 */
@Service("esService")
public class EsService {
    private static final Logger logger = LogManager.getLogger(EsService.class);

    /**
     * 用于检索任务中存在的所有ci kpi id
     *
     * @param client es客户端
     * @return String
     * @throws IOException e
     */
    public String getAllJobCi$KpiId(EsPropertiesBean client) throws IOException {
        SearchRequest searchRequest = new SearchRequest(client.getIndex()).types(client.getDocType()).scroll(new TimeValue(1, TimeUnit.MINUTES));
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        queryBuilder.must(QueryBuilders.matchQuery("type", "job"));
        sourceBuilder.query(queryBuilder);
        sourceBuilder.from(0);
        sourceBuilder.size(10000);
        //返回字段
        sourceBuilder.fetchSource(new String[]{"cis.selfCis.kpis.kpiId", "cis.selfCis.ciId"}, Strings.EMPTY_ARRAY);
        //排序
        sourceBuilder.sort(new FieldSortBuilder("createDate").order(SortOrder.ASC));
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        searchRequest.source(sourceBuilder);
        SearchResponse searchResponse = client.getEsClient().getRestHighLevelClient().search(searchRequest, client.getEsClient().getRequestOptions());
        //处理搜索命中文档结果
        SearchHits hits = searchResponse.getHits();
        if (0 == hits.totalHits) {
            return null;
        } else {
            SearchHit[] searchHits = hits.getHits();
            StringBuilder builder = new StringBuilder("[");
            Arrays.stream(searchHits).forEach(s -> builder.append(s.getSourceAsString()).append(","));
            builder.append("]");
            return builder.toString();
        }
    }

    /**
     * 用于混合查询，分别查询vic的历史数据和容量的预测数据，跨越了2个index
     *
     * @param pageBean  分页数据
     * @param clientMap 包含了基础数据的Esclient和capacity的EsClient
     * @return 基于vic的历史数据和基于容量的预测数据
     * @throws Exception 抛出异常
     */
    public UnionSource getUnionSource(PageBean pageBean, Map<String, EsPropertiesBean> clientMap) throws Exception {
        final UnionSource[] unionSources = new UnionSource[1];
        final UnionSource unionSource = new UnionSource();
        CompletableFuture.runAsync(() -> {
            //查询base 库
            pageBean.setFrom(0);
            pageBean.setPageSize(800);
            pageBean.setOrderField("arisingTime");
            pageBean.setOrderType(0);
            pageBean.setIncludeField(EsSetting.BASE_DATA_NEED_FIELD.getKey());
            //得出的历史结果
            Use2ShowPageData baseData = null;
            try {
                baseData = getBaseDataByCondition(pageBean, clientMap.get("base"));
            } catch (Exception e) {
                logger.error("查询基础数据发生异常，异常是：" + e.getMessage(), e);
            }
            //not null
            if (Objects.nonNull(baseData)) {
                Collections.reverse(baseData.getListData());
                unionSource.setVicSource(baseData.getListData());
            }
        }).runAfterBoth(CompletableFuture.runAsync(() -> {
            CapacitySource capacitySource = new CapacitySource();
            //查询容量
            Long ciId = pageBean.getCiId();
            Long kpiCode = pageBean.getKpiCode();
            Assert.notNull(ciId, "ciId不能是null！！");
            Assert.notNull(kpiCode, "kpiCode不能是null！！");
            EsPropertiesBean capacityClient = clientMap.get("capacity");
            RestHighLevelClient highLevelClient = capacityClient.getEsClient().getRestHighLevelClient();
            SearchRequest searchRequest = new SearchRequest(capacityClient.getIndex());
            searchRequest.types(capacityClient.getDocType()).scroll(new TimeValue(1, TimeUnit.MINUTES));
            //bool query builder
            BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
            //置信区间上界线数据
            queryBuilder.must(QueryBuilders.termsQuery("type", EsSetting.CAPACITY_UPPER_BOUNDARY_LINE.getKey(),
                    EsSetting.CAPACITY_LOWER_BOUNDARY_LINE.getKey(), EsSetting.CAPACITY_RESULT.getKey()))
                    //ci id
                    .must(QueryBuilders.matchQuery("ciId", ciId))
                    //kpi code
                    .must(QueryBuilders.matchQuery("kpiId", kpiCode))
                    //instance
                    .must(QueryBuilders.matchQuery("instanceId", pageBean.getInstance()))
                    //区间
                    .filter(QueryBuilders.rangeQuery("forecastDateTime").gte(pageBean.getEndAt()));
            //source builder
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            sourceBuilder.query(queryBuilder);
            sourceBuilder.from(0);
            sourceBuilder.size(600);
            //返回字段
            sourceBuilder.fetchSource(EsSetting.CAPACITY_VIC_NEED_FIELD.getKey().split(","), Strings.EMPTY_ARRAY);
            //排序
            sourceBuilder.sort(new FieldSortBuilder("forecastDateTime").order(SortOrder.ASC));
            //超时
            sourceBuilder.timeout(new TimeValue(20, TimeUnit.SECONDS));
            //add source
            searchRequest.source(sourceBuilder);
            SearchResponse searchResponse;
            try {
                searchResponse = highLevelClient.search(searchRequest, capacityClient.getEsClient().getRequestOptions());
                if (RestStatus.OK == searchResponse.status()) {
                    if (0 == searchResponse.getFailedShards()) {
                        if (0L == searchResponse.getHits().totalHits) {
                            return;
                        } else {
                            SearchHit[] searchHits = searchResponse.getHits().getHits();
                            //置信区间上界线数据
                            capacitySource.setUpperBoundaryLine(Arrays.stream(searchHits)
                                    .map(SearchHit::getSourceAsMap)
                                    .filter(hit -> EsSetting.CAPACITY_UPPER_BOUNDARY_LINE.getKey().equals(hit.get("type")))
                                    .collect(Collectors.toList()));
                            //置信区间下界线数据
                            capacitySource.setLowerBoundaryLine(Arrays.stream(searchHits)
                                    .map(SearchHit::getSourceAsMap)
                                    .filter(hit -> EsSetting.CAPACITY_LOWER_BOUNDARY_LINE.getKey().equals(hit.get("type")))
                                    .collect(Collectors.toList()));
                            //预测数据
                            capacitySource.setCapacityResult(Arrays.stream(searchHits)
                                    .map(SearchHit::getSourceAsMap)
                                    .filter(hit -> EsSetting.CAPACITY_RESULT.getKey().equals(hit.get("type")))
                                    .collect(Collectors.toList()));
                        }
                    } else {
                        Arrays.stream(searchResponse.getShardFailures()).forEach(s ->
                                logger.error("为展示容量线图，查询Es发生异常：" + s.reason()));
                        throw new RuntimeException("为展示容量线图，查询Es发生异常！！");
                    }
                } else {
                    logger.error("为展示容量线图，查询ES发生异常！！！错误code是：" + searchResponse.status());
                    throw new RuntimeException("为展示容量线图，查询ES发生异常，异常code是：" + searchResponse.status());
                }
                //set capacity source
                unionSource.setCapacitySource(capacitySource);
            } catch (IOException e) {
                logger.error("查询容量结果发生异常，异常是：" + e.getMessage(), e);
            }
        }), () -> unionSources[0] = unionSource).whenComplete((i, e) -> {
            if (Objects.nonNull(e)) {
                unionSources[0] = null;
            }
        }).join();

        return unionSources[0];
    }

    /**
     * 用于同时新建、部分更新操作
     *
     * @param jsonString json串
     * @param docIdMap   doc id map
     * @param client     es相关属性对象
     */
    public void bulkService(String jsonString, Map<String, String> docIdMap, EsPropertiesBean client,
                            String scriptCode, Map<String, Object> scriptMap) throws Exception {
        BulkRequest request = new BulkRequest();
        //insert
        request.add(new IndexRequest(client.getIndex(), client.getDocType(), docIdMap.get("createDocId")).source(jsonString, XContentType.JSON));
        //part update
        request.add(new UpdateRequest(client.getIndex(), client.getDocType(), docIdMap.get("updateDocId")).scriptedUpsert(true)
                .script(new Script(ScriptType.INLINE,
                        Script.DEFAULT_SCRIPT_LANG,
                        scriptCode,
                        scriptMap)));

        request.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
        BulkResponse bulkResponse = client.getEsClient().getRestHighLevelClient().bulk(request, client.getEsClient().getRequestOptions());
        //处理请求
        if (Objects.nonNull(bulkResponse)) {
            for (BulkItemResponse bulkItemResponse : bulkResponse) {
                DocWriteResponse itemResponse = bulkItemResponse.getResponse();
                if (bulkItemResponse.getOpType() == DocWriteRequest.OpType.INDEX
                        || bulkItemResponse.getOpType() == DocWriteRequest.OpType.CREATE) {
                    IndexResponse indexResponse = (IndexResponse) itemResponse;
                    if (0 != indexResponse.getShardInfo().getFailed()) {
                        Arrays.stream(indexResponse.getShardInfo().getFailures()).forEach(failure ->
                                logger.error("向es更新数据发生错误！错误是：" + failure.reason()));
                        throw new RuntimeException("向es更新数据发生错误！");
                    }
                    if (DocWriteResponse.Result.CREATED != indexResponse.getResult()) {
                        logger.error("向index是[" + client.getIndex() + "]type是：[" + client.getDocType() + "]es中添加文档失败，状态码是：【" + indexResponse.status().getStatus() + "】");
                        throw new RuntimeException("向es更新数据发生错误！Code是：" + indexResponse.status());
                    }

                } else if (bulkItemResponse.getOpType() == DocWriteRequest.OpType.UPDATE) {
                    UpdateResponse updateResponse = (UpdateResponse) itemResponse;
                    if (0 != updateResponse.getShardInfo().getFailed()) {
                        Arrays.stream(updateResponse.getShardInfo().getFailures()).forEach(f ->
                                logger.error("通过脚本更新一条数据的部分属性发生错误，错误是：" + f.reason()));
                        throw new RuntimeException("过脚本更新一条数据的部分属性发生异常！！");
                    }
                    if (DocWriteResponse.Result.UPDATED != updateResponse.getResult()) {
                        logger.error("部分更新docId是[" + docIdMap.get("updateDocId") + "]的文档失败，失败code是[" + updateResponse.status().getStatus() + "]！！！");
                    }
                }
            }
        }

    }

    /**
     * 通过ciId kpiCode instance分页检索保存在es中的基础数据。
     *
     * @param pageBean 分页对象
     * @param client   文档所在索引
     * @return 查询结果
     * @throws Exception e
     */
    public Use2ShowPageData getBaseDataByCondition(PageBean pageBean, EsPropertiesBean client) throws Exception {
        //ci id
        Long ciId = pageBean.getCiId();
        //kpi
        Long kpiCode = pageBean.getKpiCode();
        Assert.notNull(ciId, "ciId不能是null！！！");
        Assert.notNull(kpiCode, "kpiCode不能是null！！！");
        //form
        Integer from = pageBean.getFrom();
        //size
        Integer pageSize = pageBean.getPageSize();
        Assert.isTrue(from != null && pageSize != null && pageSize != 0, "from不能是null，pageSize不能是null，pageSize不能是0！！！");
        //order field
        String orderField = pageBean.getOrderField();
        Assert.hasText(orderField, "orderField不能是null/''！！！！");
        //order type
        Integer orderType = pageBean.getOrderType();
        Assert.isTrue(orderType != null && (orderType == 0 || orderType == 1), "orderType不能是null，并且只能是0或者1！！");
        //high level client
        RestHighLevelClient highLevelClient = client.getEsClient().getRestHighLevelClient();
        SearchRequest searchRequest;
        //如果跨越索引
        if (StringUtils.isNotBlank(pageBean.getPartOfIndexes())) {
            searchRequest = new SearchRequest(Arrays.stream(pageBean.getPartOfIndexes().split(","))
                    .filter(StringUtils::isNotBlank)
                    .map(s -> client.getIndex() + s)
                    .collect(Collectors.toList())
                    .toArray(new String[]{}));
        } else {
            searchRequest = new SearchRequest(client.getIndex());
        }
        searchRequest.types(client.getDocType()).scroll(new TimeValue(1, TimeUnit.MINUTES));
        //bool query builder
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        queryBuilder.must(QueryBuilders.matchQuery("nodeId", ciId))
                .must(QueryBuilders.matchQuery("kpiCode", kpiCode));
        if (StringUtils.isNotBlank(pageBean.getInstance()))
            queryBuilder.must(QueryBuilders.matchQuery("instance", pageBean.getInstance()));
        String startAt = pageBean.getStartAt();
        String endAt = pageBean.getEndAt();
        if (StringUtils.isNotBlank(startAt) && StringUtils.isBlank(endAt)) {
            queryBuilder.filter(QueryBuilders.rangeQuery("arisingTime").gte(startAt));
        } else if (StringUtils.isBlank(startAt) && StringUtils.isNotBlank(endAt)) {
            queryBuilder.filter(QueryBuilders.rangeQuery("arisingTime").lte(endAt));
        } else if (StringUtils.isNotBlank(startAt) && StringUtils.isNotBlank(endAt)) {
            queryBuilder.filter(QueryBuilders.rangeQuery("arisingTime")
                    .gte(startAt)
                    .lte(endAt));
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
                return getReturnData(searchResponse);
            } else {
                Arrays.stream(searchResponse.getShardFailures()).forEach(s ->
                        logger.error("分页获得基础数据对象发生异常：" + s.reason()));
                throw new RuntimeException("分页获得基础数据对象发生异常！！");
            }
        } else {
            logger.error("查询基础数据发生异常！！！错误code是：" + searchResponse.status());
            throw new RuntimeException("查询基础数据发生异常，异常code是：" + searchResponse.status());
        }
    }

    /**
     * 获得相关性线图展示数据
     *
     * @param searchCorrelationLineBean 查询条件
     * @param client                    es客户端
     * @return 查询结果
     * @throws Exception 异常
     */
    public Use2CorrelationLine getBaseDataUse2CorrelationLine(SearchCorrelationLineBean searchCorrelationLineBean
            , EsPropertiesBean client) throws Exception {
        String endAt = searchCorrelationLineBean.getEndAt();
        Assert.hasText(endAt, "结束时间不能是null/''!!!");
        String mainIds = searchCorrelationLineBean.getMainIds();
        Assert.hasText(mainIds, "mainIds不能是null/''!!!");
        String subIds = searchCorrelationLineBean.getSubIds();
        Assert.hasText(subIds, "mainIds不能是null/''!!!");
        String startAt = LocalDateTimeUtil.String2DateTime(endAt)
                .minusDays(3)
                .format(DateTimeFormatter.ofPattern("u-MM-dd HH:mm:ss"));
        SearchRequest searchRequest = new SearchRequest(client.getIndex());
        searchRequest.types(client.getDocType()).scroll(new TimeValue(10, TimeUnit.SECONDS));
        //bool query builder
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        //查询条件 main id
        String mainId[] = mainIds.split(",");
        Assert.notEmpty(mainId, "mainId不能是null/[]！！");
        Assert.isTrue(3 == mainId.length, "mainId通过‘,’分割出来的长度必须是3！！");
        String mainNodeId = mainId[0];
        //sub id
        String subId[] = subIds.split(",");
        Assert.notEmpty(subId, "subId不能是null/[]！！");
        String subNodeId = subId[0];
        Assert.isTrue(3 == subId.length, "subId通过‘,’分割出来的长度必须是3！！");
        queryBuilder.must(QueryBuilders.termsQuery("nodeId", new BigDecimal(mainNodeId).longValue(), new BigDecimal(subNodeId).longValue()))
                .must(QueryBuilders.termsQuery("kpiCode", new BigDecimal(mainId[1]).longValue(), new BigDecimal(subId[1]).longValue()))
                .must(QueryBuilders.termsQuery("instance", mainId[2], subId[2]));
        //时间区间
        queryBuilder.filter(QueryBuilders.rangeQuery("arisingTime")
                .gte(startAt)
                .lte(endAt));
        //source builder
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(queryBuilder);
        sourceBuilder.from(0);
        sourceBuilder.size(6000);
        sourceBuilder.fetchSource(new String[]{"arisingTime", "value", "nodeId"}, Strings.EMPTY_ARRAY);
        sourceBuilder.sort(new FieldSortBuilder("arisingTime").order(SortOrder.ASC));
        sourceBuilder.timeout(new TimeValue(20, TimeUnit.SECONDS));
        //add source
        searchRequest.source(sourceBuilder);
        SearchResponse searchResponse = client.getEsClient().getRestHighLevelClient()
                .search(searchRequest, client.getEsClient().getRequestOptions());
        if (RestStatus.OK == searchResponse.status()) {
            if (0 == searchResponse.getFailedShards()) {
                if (0L == searchResponse.getHits().totalHits) {
                    return null;
                } else {
                    List<Map<String, Object>> source = new ArrayList<>();
                    SearchHit[] searchHits = searchResponse.getHits().getHits();
                    Arrays.stream(searchHits).forEach(s -> source.add(s.getSourceAsMap()));
                    Use2CorrelationLine line = new Use2CorrelationLine();
                    //set main
                    line.setMain(source.stream()
                            .filter(map -> (long) map.get("nodeId") == new BigDecimal(mainNodeId).longValue())
                            .collect(Collectors.toList()));
                    line.setSub(source.stream().filter(map -> (long) map.get("nodeId") == new BigDecimal(subNodeId).longValue())
                            .collect(Collectors.toList()));
                    return line;
                }
            } else {
                Arrays.stream(searchResponse.getShardFailures()).forEach(s ->
                        logger.error("分页获得基础数据对象发生异常：" + s.reason()));
                throw new RuntimeException("分页获得基础数据对象发生异常！！");
            }
        } else {
            logger.error("查询基础数据发生异常！！！错误code是：" + searchResponse.status());
            throw new RuntimeException("查询基础数据发生异常，异常code是：" + searchResponse.status());
        }
    }

    /**
     * 从es中，删除一条数据！
     *
     * @param docId  文档id
     * @param client 文档所在索引
     * @throws Exception e
     */
    public void deleteDoc(String docId, EsPropertiesBean client) throws Exception {
        RestHighLevelClient highLevelClient = client.getEsClient().getRestHighLevelClient();
        DeleteRequest deleteRequest = new DeleteRequest(client.getIndex(), client.getDocType(), docId);
        deleteRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
        DeleteResponse deleteResponse = highLevelClient.delete(deleteRequest, client.getEsClient().getRequestOptions());
        if (DocWriteResponse.Result.DELETED != deleteResponse.getResult() || DocWriteResponse.Result.NOT_FOUND != deleteResponse.getResult())
            throw new RuntimeException("从es删除id是[" + docId + "]的文档失败！！");
    }

    /**
     * 批量删除
     *
     * @param ids    要删除的id集合
     * @param client es客户端
     * @throws Exception 异常抛出
     */
    public void bulkDelete(String[] ids, EsPropertiesBean client) throws Exception {
        BulkRequest request = new BulkRequest();
        Arrays.stream(ids).forEach(id -> request.add(new DeleteRequest(client.getIndex(), client.getDocType(), id)));
        request.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
        BulkResponse responses = client.getEsClient().getRestHighLevelClient()
                .bulk(request, client.getEsClient().getRequestOptions());
        if (responses.hasFailures()) {
            throw new ElasticsearchCorruptionException("批量删除发生异常！！消息是：" + responses.buildFailureMessage());
        } else {
            Arrays.stream(responses.getItems()).map(BulkItemResponse::getResponse)
                    .map(docResponse -> (DeleteResponse) docResponse)
                    .forEach(i -> logger.info("删除的结果是：【" + i.getResult() + "】"));
        }
    }

    /**
     * 给导出excel准备数据
     *
     * @param pageBean 分页对象
     * @param client   文档所在索引
     * @return map List
     * @throws Exception e
     */
    public List<Map<String, Object>> getExcelData(PageBean pageBean, EsPropertiesBean client) throws Exception {
        String historyJobId = pageBean.getHistoryJobId();
        Assert.hasText(historyJobId, () -> "导出excel，historyJobId不能是null/''！！！");
        String orderField = pageBean.getOrderField();
        Assert.hasText(orderField, "orderField不能是null/''！！！！");
        Integer orderType = pageBean.getOrderType();
        Assert.isTrue(orderType != null && (orderType == 0 || orderType == 1), "orderType不能是null，并且只能是0或者1！！");
        RestHighLevelClient highLevelClient = client.getEsClient().getRestHighLevelClient();
        //bool query builder
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        //未删除
        queryBuilder.must(QueryBuilders.matchQuery("type", pageBean.getSettingMap().get("type").getKey()))
                .must(QueryBuilders.matchQuery("belongedTo", historyJobId));
        //模糊检索
        String searchName = pageBean.getSearchName();
        if (StringUtils.isNotBlank(searchName)) {
            queryBuilder.must(QueryBuilders.multiMatchQuery(searchName, pageBean.getSettingMap().get("fuzzy").getKey().split(",")));
        }
        //source builder
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(queryBuilder);
        sourceBuilder.from(0);
        sourceBuilder.size(ExcelSetting.max_count.getValue());
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
        //search request
        SearchRequest searchRequest = new SearchRequest(client.getIndex());
        searchRequest.types(client.getDocType());
        //add source
        searchRequest.source(sourceBuilder);
        SearchResponse searchResponse = highLevelClient.search(searchRequest, client.getEsClient().getRequestOptions());
        //返回结果
        List<Map<String, Object>> maps = new ArrayList<>();
        if (RestStatus.OK == searchResponse.status()) {
            if (0 == searchResponse.getFailedShards()) {
                long totalHits = searchResponse.getHits().totalHits;
                if (0L == totalHits) {
                    return null;
                } else {
                    SearchHit[] searchHits = searchResponse.getHits().getHits();
                    Arrays.stream(searchHits).forEach(hit -> maps.add(hit.getSourceAsMap()));
                    return maps;
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

    /**
     * 查看文档名字是否存在
     *
     * @param jobName job名称
     * @param client  文档所在索引
     * @return result
     * @throws Exception exception
     */
    public boolean checkJobNameExists(String jobName, EsPropertiesBean client) throws Exception {
        RestHighLevelClient highLevelClient = client.getEsClient().getRestHighLevelClient();
        SearchRequest searchRequest = new SearchRequest(client.getIndex());
        searchRequest.types(client.getDocType());
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        queryBuilder.must(QueryBuilders.matchQuery("type", EsSetting.JOB.getKey()))
                .must(QueryBuilders.matchQuery("jobNameKeyWord", jobName));
        //source builder
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.timeout(new TimeValue(5, TimeUnit.SECONDS));
        //add query
        sourceBuilder.query(queryBuilder);
        //add source
        searchRequest.source(sourceBuilder);
        SearchResponse searchResponse = highLevelClient.search(searchRequest, client.getEsClient().getRequestOptions());
        if (RestStatus.OK == searchResponse.status()) {
            if (0 == searchResponse.getFailedShards()) {
                return 0L == searchResponse.getHits().totalHits;
            } else {
                Arrays.stream(searchResponse.getShardFailures()).forEach(s ->
                        logger.error("检索任务名称是否存在发生异常是：" + s.reason()));
                throw new RuntimeException("检索任务名称是否存在发生异常！！");
            }
        } else {
            logger.error("查询发生异常！！！错误code是：" + searchResponse.status());
            throw new RuntimeException("检索任务名称是否存在发生异常！Code是：" + searchResponse.status());
        }
    }

    /**
     * 向es中，插入一条数据
     *
     * @param jsonString 要插入es数据库的json串
     * @param docId      该文档的id
     * @param client     文档所在索引
     */
    public void insertOrUpdateData(String jsonString, String docId, EsPropertiesBean client) throws Exception {
        RestHighLevelClient highLevelClient = client.getEsClient().getRestHighLevelClient();
        IndexRequest indexRequest = new IndexRequest(client.getIndex(), client.getDocType(), docId);
        indexRequest.source(jsonString, XContentType.JSON);
        indexRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
        IndexResponse indexResponse = highLevelClient.index(indexRequest, client.getEsClient().getRequestOptions());
        if (0 != indexResponse.getShardInfo().getFailed()) {
            Arrays.stream(indexResponse.getShardInfo().getFailures()).forEach(failure ->
                    logger.error("向es更新、插入数据发生错误！错误是：" + failure.reason()));
            throw new RuntimeException("向es更新、插入数据发生错误！");
        }
        if (DocWriteResponse.Result.CREATED != indexResponse.getResult() &&
                DocWriteResponse.Result.UPDATED != indexResponse.getResult()) {
            logger.error("向index是[" + client.getIndex() + "]type是：[" + client.getDocType() + "]es中添加文档失败，状态码是：【" + indexResponse.status().getStatus() + "】");
            throw new RuntimeException("向es更新、插入数据发生错误！Code是：" + indexResponse.status());
        }
    }

    /**
     * 通过doc id获得一条数据
     *
     * @param docID  该文档的id
     * @param client 文档所在索引
     * @return json串
     */
    public Map<String, Object> getDataWithId(String docID, EsPropertiesBean client) throws Exception {
        GetRequest request = new GetRequest(client.getIndex(), client.getDocType(), docID);
        GetResponse response = client.getEsClient().getRestHighLevelClient().get(request,
                client.getEsClient().getRequestOptions());
        if (response.isExists()) {
            return response.getSourceAsMap();
        } else {
            return null;
        }
    }

    /**
     * 通过doc id获得一条数据，这个方法专门给实现了Job的类使用，方便对象转换
     *
     * @param docID  该文档的id
     * @param client 文档所在索引
     * @return json串
     */
    public String getDataWithId2(String docID, EsPropertiesBean client) throws Exception {
        GetRequest request = new GetRequest(client.getIndex(), client.getDocType(), docID);
        GetResponse response = client.getEsClient().getRestHighLevelClient().get(request,
                client.getEsClient().getRequestOptions());
        if (response.isExists()) {
            return response.getSourceAsString();
        } else {
            return null;
        }
    }

    private Use2ShowPageData getReturnData(SearchResponse searchResponse) {
        if (0L == searchResponse.getHits().totalHits) {
            return null;
        } else {
            List<Map<String, Object>> source = new ArrayList<>();
            SearchHit[] searchHits = searchResponse.getHits().getHits();
            Arrays.stream(searchHits).forEach(s -> source.add(s.getSourceAsMap()));
            return new Use2ShowPageData(source, searchResponse.getHits().totalHits);
        }
    }

    /**
     * 部分更新/添加一条文档属性。
     *
     * @param docID      该文档的id
     * @param client     文档所在索引
     * @param scriptCode 脚本语句
     * @param scriptMap  对应语句的map
     * @return 成功/失败
     */
    public boolean updateOrAddDocField(String docID, String scriptCode, Map<String, Object> scriptMap,
                                       EsPropertiesBean client) throws Exception {
        Assert.hasText(docID, () -> "要删除的doc的id，不能是null/''！！！");
        Assert.isTrue(StringUtils.isNotBlank(client.getIndex()) &&
                StringUtils.isNotBlank(client.getDocType()) &&
                StringUtils.isNotBlank(scriptCode), "client.getIndex()，client.getDocType()，scriptCode不能是null/''！！");
        Assert.notEmpty(scriptMap, () -> "scriptMap不能是empty！！！！");
        RestHighLevelClient highLevelClient = client.getEsClient().getRestHighLevelClient();
        UpdateRequest updateRequest = new UpdateRequest(client.getIndex(), client.getDocType(), docID);
        updateRequest.scriptedUpsert(true);
        updateRequest.script(new Script(ScriptType.INLINE,
                Script.DEFAULT_SCRIPT_LANG,
                scriptCode,
                scriptMap));
        updateRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
        UpdateResponse updateResponse = highLevelClient.update(updateRequest, client.getEsClient().getRequestOptions());
        if (0 != updateResponse.getShardInfo().getFailed()) {
            Arrays.stream(updateResponse.getShardInfo().getFailures()).forEach(f ->
                    logger.error("通过脚本更新一条数据的部分属性发生错误，错误是：" + f.reason()));
            throw new RuntimeException("过脚本更新一条数据的部分属性发生异常！！");
        }
        if (DocWriteResponse.Result.UPDATED == updateResponse.getResult()) {
            return true;
        } else {
            logger.warn("部分更新docId是[" + docID + "]的文档失败，失败code是[" + updateResponse.status().getStatus() + "]！！！");
            return false;
        }

    }

    /**
     * 条件分页检索
     *
     * @param pageBean 分页对象
     * @param client   文档所在索引
     * @return 得到的json
     */
    public Use2ShowPageData searchByCondition(PageBean pageBean, EsPropertiesBean client) throws Exception {
        Integer from = pageBean.getFrom();
        Integer pageSize = pageBean.getPageSize();
        Assert.isTrue(from != null && pageSize != null && pageSize != 0, "from不能是null，pageSize不能是null，pageSize不能是0！！！");
        String orderField = pageBean.getOrderField();
        Assert.hasText(orderField, "orderField不能是null/''！！！！");
        Integer orderType = pageBean.getOrderType();
        Assert.isTrue(orderType != null && (orderType == 0 || orderType == 1), "orderType不能是null，并且只能是0或者1！！");
        RestHighLevelClient highLevelClient = client.getEsClient().getRestHighLevelClient();
        SearchRequest searchRequest;
        //如果跨越索引
        if (StringUtils.isNotBlank(pageBean.getPartOfIndexes())) {
            searchRequest = new SearchRequest(Arrays.stream(pageBean.getPartOfIndexes().split(","))
                    .filter(StringUtils::isNotBlank)
                    .map(s -> client.getIndex() + s)
                    .collect(Collectors.toList())
                    .toArray(new String[]{}));
        } else {
            searchRequest = new SearchRequest(client.getIndex());
        }
        searchRequest.types(client.getDocType());
        //bool query builder
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        //任务分页检索
        if (StringUtils.isBlank(pageBean.getHistoryJobId()) && StringUtils.isBlank(pageBean.getJobId())) {
            pageBean.setIncludeField(EsSetting.JOB_INCLUDE_FIELD.getKey());
            //数据类型
            queryBuilder.must(QueryBuilders.matchQuery("type", EsSetting.JOB.getKey()));
            //任务类型
            Integer jobType = pageBean.getJobType();
            if (jobType != null && (jobType == 10 || jobType == 20))
                queryBuilder.must(QueryBuilders.matchQuery("jobType", jobType));
            //执行状态
            Integer enabled = pageBean.getEnabled();
            if (enabled != null && (enabled == 1 || enabled == 0))
                queryBuilder.must(QueryBuilders.matchQuery("enabled", enabled));
            //任务名称
            String jobName = pageBean.getJobName();
            if (StringUtils.isNotBlank(jobName))
                queryBuilder.must(QueryBuilders.wildcardQuery("jobNameKeyWord",
                        String.format(EsSetting.WILDCARD_STR.getKey(), jobName)));
            //ci 名称
            String ciName = pageBean.getCiName();
            if (StringUtils.isNotBlank(ciName))
                queryBuilder.must(QueryBuilders.nestedQuery("cis.selfCis", QueryBuilders
                        .wildcardQuery("cis.selfCis.ciLabel", String.format(EsSetting.WILDCARD_STR.getKey(), ciName)), ScoreMode.None));
            //kpi 名称
            String kpiName = pageBean.getKpiName();
            if (StringUtils.isNotBlank(kpiName))
                queryBuilder.must(QueryBuilders.nestedQuery("cis.selfCis.kpis", QueryBuilders
                        .wildcardQuery("cis.selfCis.kpis.kpiLabel", String.format(EsSetting.WILDCARD_STR.getKey(), kpiName)), ScoreMode.None));
            //时间区间 modifyDate
            String startAt = pageBean.getStartAt();
            String endAt = pageBean.getEndAt();
            if (StringUtils.isNotBlank(startAt) && StringUtils.isBlank(endAt)) {
                queryBuilder.filter(QueryBuilders.rangeQuery("modifyDate").gte(startAt));
            } else if (StringUtils.isBlank(startAt) && StringUtils.isNotBlank(endAt)) {
                queryBuilder.filter(QueryBuilders.rangeQuery("modifyDate").lte(endAt));
            } else if (StringUtils.isNotBlank(startAt) && StringUtils.isNotBlank(endAt)) {
                queryBuilder.filter(QueryBuilders.rangeQuery("modifyDate")
                        .gte(startAt)
                        .lte(endAt));
            }
        } else if (StringUtils.isNotBlank(pageBean.getHistoryJobId()) &&
                StringUtils.isBlank(pageBean.getJobId())) {//任务执行结果分页检索
            queryBuilder.must(QueryBuilders.matchQuery("type", pageBean.getSettingMap().get("type").getKey()))
                    .must(QueryBuilders.matchQuery("belongedTo", pageBean.getHistoryJobId()));
            //模糊检索
            String searchName = pageBean.getSearchName();
            if (StringUtils.isNotBlank(searchName)) {
                //子检索
                BoolQueryBuilder query = QueryBuilders.boolQuery();
                Arrays.stream(pageBean.getSettingMap().get("fuzzy").getKey().split(",")).forEach(s ->
                        query.should(QueryBuilders.wildcardQuery(s, String.format(EsSetting.WILDCARD_STR.getKey(), searchName))));
                queryBuilder.must(query);
            }
        } else if (StringUtils.isBlank(pageBean.getHistoryJobId()) &&
                StringUtils.isNotBlank(pageBean.getJobId())) {//任务历史分页检索
            pageBean.setIncludeField(EsSetting.JOB_INCLUDE_FIELD.getKey());
            queryBuilder.must(QueryBuilders.matchQuery("type", EsSetting.JOB_HISTORY.getKey()))
                    .must(QueryBuilders.matchQuery("belongedTo", pageBean.getJobId()));
        } else {
            logger.warn("不应该存在的情况，请检查代码！！！！");
            return null;
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
                    return getReturnData(searchResponse);
                }
            } else {
                Arrays.stream(searchResponse.getShardFailures()).forEach(s ->
                        logger.error("检索任务发生异常是：" + s.reason()));
                throw new RuntimeException("分页获得对象发生异常！！");
            }
        } else {
            logger.error("查询发生异常！！！错误code是：" + searchResponse.status());
            throw new RuntimeException("查询发生异常，异常code是：" + searchResponse.status());
        }
    }

    /**
     * 更新/添加多条数据的相同的属性
     *
     * @param client     文档所在索引
     * @param scriptCode 脚本语句
     * @param scriptMap  对应语句的map
     * @param docIds     要删除的doc的id
     */
    public void bulkUpdateOrAddDocField(String scriptCode, Map<String, Object> scriptMap, String[] docIds,
                                        EsPropertiesBean client) throws Exception {
        Assert.notEmpty(docIds, () -> "要删除的doc的id数组，不能是null/length==0");
        Assert.isTrue(StringUtils.isNotBlank(client.getIndex()) &&
                StringUtils.isNotBlank(client.getDocType()) &&
                StringUtils.isNotBlank(scriptCode), "client.getIndex()，client.getDocType()，scriptCode不能是null/''！！");
        Assert.notEmpty(scriptMap, () -> "scriptMap不能是empty！！！！");
        RestHighLevelClient highLevelClient = client.getEsClient().getRestHighLevelClient();
        //bulk request
        BulkRequest request = new BulkRequest();
        Arrays.stream(docIds).forEach(docId -> request.add(new UpdateRequest(client.getIndex(), client.getDocType(), docId)
                .scriptedUpsert(true)
                .script(new Script(ScriptType.INLINE,
                        Script.DEFAULT_SCRIPT_LANG,
                        scriptCode,
                        scriptMap))
        ));
        request.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
        BulkResponse bulkResponse = highLevelClient.bulk(request, client.getEsClient().getRequestOptions());
        if (bulkResponse.hasFailures()) {
            logger.error(bulkResponse.buildFailureMessage());
            throw new RuntimeException("通过script脚本更新es字段发生错误！！");
        }
    }

    /**
     * 通过id，批量检索
     *
     * @param ids          文档id
     * @param client       es客户端
     * @param enable       是否启用
     * @param includeField 返回字段数组
     * @return 对象集合
     * @throws Exception 异常抛出
     */
    public List<QuartzJobBean> searchByKeys(String[] ids, EsPropertiesBean client, Integer enable,
                                            String[] includeField) throws Exception {
        SearchRequest request = new SearchRequest(client.getIndex());
        request.types(client.getDocType());
        BoolQueryBuilder builder = QueryBuilders.boolQuery();
        builder.must(QueryBuilders.matchQuery("type", EsSetting.JOB.getKey()))
                .must(QueryBuilders.matchQuery("enabled", enable))
                .must(QueryBuilders.termsQuery("jobId", ids));
        //source builder
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(builder);
        sourceBuilder.from(0);
        sourceBuilder.size(100);
        //要求返回固定对象中的某些字段
        sourceBuilder.fetchSource(includeField, Strings.EMPTY_ARRAY);
        sourceBuilder.timeout(new TimeValue(10, TimeUnit.SECONDS));
        //add source
        request.source(sourceBuilder);
        SearchResponse searchResponse = client.getEsClient().getRestHighLevelClient().search(
                request, client.getEsClient().getRequestOptions());
        if (RestStatus.OK == searchResponse.status()) {
            if (0 == searchResponse.getFailedShards()) {
                return getQuartzJobBeans(searchResponse);
            } else {
                Arrays.stream(searchResponse.getShardFailures()).forEach(s ->
                        logger.error("检索任务发生异常是：" + s.reason()));
                throw new RuntimeException("分页获得对象发生异常！！");
            }
        } else {
            logger.error("查询发生异常！！！错误code是：" + searchResponse.status());
            throw new RuntimeException("查询发生异常，异常code是：" + searchResponse.status());
        }
    }

    /**
     * 用来给项目重新启动的时候，查询在启动周期之内的任务。用于启动定时。
     * 当jobType是null的时候，是给容量定时，非null的时候给相关性定时
     *
     * @param jobType      任务类型，可空
     * @param client       es客户端
     * @param includeField 返回字段
     * @return 定时对象
     * @throws IOException 异常抛出
     */
    public List<QuartzJobBean> use2Startup(@Nullable Integer jobType, EsPropertiesBean client, String[] includeField) throws IOException {
        SearchRequest request = new SearchRequest(client.getIndex());
        request.types(client.getDocType()).scroll(new TimeValue(1, TimeUnit.MINUTES));
        BoolQueryBuilder builder = QueryBuilders.boolQuery();
        //容量
        builder.must(QueryBuilders.matchQuery("type", EsSetting.JOB.getKey()))
                .must(QueryBuilders.matchQuery("enabled", 1));
        if (Objects.nonNull(jobType)) {//相关性
            Assert.isTrue(10 == jobType || 20 == jobType, "jobType只能是10/20！！");
            switch (jobType) {
                case 10://单次任务
                    builder.must(QueryBuilders.matchQuery("jobType", 10))
                            .filter(QueryBuilders.rangeQuery("singleExecutionTime")
                                    .gte(LocalDateTime.now().format(DateTimeFormatter.ofPattern("u-MM-dd HH:mm:ss"))));
                    break;
                case 20://周期任务
                    builder.must(QueryBuilders.matchQuery("jobType", 20));
                    break;
                default:
                    return null;
            }
        }
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(builder);
        sourceBuilder.from(0);
        sourceBuilder.size(2000);
        //要求返回固定对象中的某些字段
        sourceBuilder.fetchSource(includeField, Strings.EMPTY_ARRAY);
        sourceBuilder.timeout(new TimeValue(10, TimeUnit.SECONDS));
        //add source
        request.source(sourceBuilder);
        SearchResponse searchResponse = client.getEsClient().getRestHighLevelClient().search(
                request, client.getEsClient().getRequestOptions());
        if (RestStatus.OK == searchResponse.status()) {
            if (0 == searchResponse.getFailedShards()) {
                return getQuartzJobBeans(searchResponse);
            } else {
                Arrays.stream(searchResponse.getShardFailures()).forEach(s ->
                        logger.error("启动项目，检索任务发生异常：" + s.reason()));
                throw new RuntimeException("启动项目，检索任务发生异常！！");
            }
        } else {
            logger.error("查询发生异常！！！错误code是：" + searchResponse.status());
            throw new RuntimeException("查询发生异常，异常code是：" + searchResponse.status());
        }
    }

    /**
     * 返回List<QuartzJobBean>类型的查询
     *
     * @param searchResponse 查询结果
     * @return List<QuartzJobBean>
     */
    private List<QuartzJobBean> getQuartzJobBeans(SearchResponse searchResponse) {
        if (0L == searchResponse.getHits().totalHits) {
            return null;
        } else {
            List<QuartzJobBean> list = new ArrayList<>();
            Arrays.stream(searchResponse.getHits().getHits())
                    .forEach(s -> list.add(JSONObject.parseObject(s.getSourceAsString(), QuartzJobBean.class)));
            return list;
        }
    }
}