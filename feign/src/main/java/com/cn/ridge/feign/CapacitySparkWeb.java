package com.cn.ridge.feign;

import com.cn.ridge.bean.CorrelationWebBean;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Author: create by wang.gf
 * Date: create at 2018/12/24
 */
@FeignClient(url = "${capacity.web.url}", name = "${capacity.web.name}")
public interface CapacitySparkWeb {
    @RequestMapping(value = "/sparkWeb/api/spark/submit/capacityForecast", method = RequestMethod.POST)
    String call(@RequestBody CorrelationWebBean bean);
}
