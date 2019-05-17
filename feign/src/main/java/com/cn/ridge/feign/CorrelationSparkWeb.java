package com.cn.ridge.feign;

import com.cn.ridge.bean.CorrelationWebBean;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Author: create by wang.gf
 * Date: create at 2018/11/29
 */
@FeignClient(url = "${correlation.web.url}", name = "${correlation.web.name}")
public interface CorrelationSparkWeb {
    @RequestMapping(value = "/jersey-REST_war/api/spark2/pearson2", method = RequestMethod.POST)
    String call(@RequestBody CorrelationWebBean bean);
}
