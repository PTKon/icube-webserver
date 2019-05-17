package com.cn.ridge.bean;

import java.util.List;
import java.util.Map;

/**
 * Author: create by wang.gf
 * Date: create at 2019/1/18
 */
public class Use2CorrelationLine {
    private List<Map<String, Object>> main;
    private List<Map<String, Object>> sub;

    public List<Map<String, Object>> getMain() {
        return main;
    }

    public void setMain(List<Map<String, Object>> main) {
        this.main = main;
    }

    public List<Map<String, Object>> getSub() {
        return sub;
    }

    public void setSub(List<Map<String, Object>> sub) {
        this.sub = sub;
    }
}
