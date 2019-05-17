package com.cn.ridge.controller;

import com.cn.ridge.base.Form;
import com.cn.ridge.bean.*;
import com.cn.ridge.enums.ReturnCode;
import com.cn.ridge.es.EsClient;
import com.cn.ridge.es.EsInitBean;
import com.cn.ridge.service.EsService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * Author: create by wang.gf
 * Date: create at 2018/12/6
 */
@RestController
public class BaseDataController {
    private final EsService esService;
    private static final Logger logger = LogManager.getLogger(BaseDataController.class);

    /**
     * 基础分页查询
     *
     * @param pageBean 查询条件
     * @return 查询结果
     */
    @GetMapping(value = "/docs")
    public Form<Use2ShowPageData> getInstances(PageBean pageBean) {
        Use2ShowPageData jsonObject;
        try {
            jsonObject = esService.getBaseDataByCondition(pageBean, new EsPropertiesBean(EsInitBean.getIndex(), EsInitBean.getType(), EsClient.client));
        } catch (Exception e) {
            logger.error("在es中检索基础数据发生错误！", e);
            return new Form<>(ReturnCode.error, "在es中检索基础数据发生错误！");
        }
        if (Objects.nonNull(jsonObject))
            return new Form<>(ReturnCode.success, "检索成功！", jsonObject);
        else
            return new Form<>(ReturnCode.notFound, "通过检索条件，没有检索到人任何数据！");
    }

    /**
     * 用于给相关性展示线图
     *
     * @param bean 查询条件
     * @return 查询结果
     */
    @GetMapping(value = "/correlationLine")
    public Form<Use2CorrelationLine> getCorrelationLineData(SearchCorrelationLineBean bean) {
        Use2CorrelationLine line;
        try {
            line = esService.getBaseDataUse2CorrelationLine(bean,
                    new EsPropertiesBean(EsInitBean.getIndex(), EsInitBean.getType(), EsClient.client));
        } catch (Exception e) {
            logger.error("在es中检索基础数据发生错误！", e);
            return new Form<>(ReturnCode.error, "在es中检索基础数据发生错误！");
        }
        if (Objects.nonNull(line))
            return new Form<>(ReturnCode.success, "检索成功！", line);
        else
            return new Form<>(ReturnCode.notFound, "通过检索条件，没有检索到人任何数据！");
    }

    @Autowired
    public BaseDataController(EsService esService) {
        this.esService = esService;
    }
}
