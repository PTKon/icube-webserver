package com.cn.ridge.bean;

import java.util.List;
import java.util.Map;

/**
 * Author: create by wang.gf
 * Date: create at 2019/1/7
 */
public class Use2ShowPageData {

    private List<Map<String,Object>> listData;
    private Long total;

    public List<Map<String, Object>> getListData() {
        return listData;
    }

    public void setListData(List<Map<String, Object>> listData) {
        this.listData = listData;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Use2ShowPageData(List<Map<String, Object>> listData, Long total) {
        this.listData = listData;
        this.total = total;
    }
}
