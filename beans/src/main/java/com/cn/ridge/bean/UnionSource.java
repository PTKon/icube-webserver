package com.cn.ridge.bean;

import java.util.List;
import java.util.Map;

/**
 * Author: create by wang.gf
 * Date: create at 2018/12/29
 */
public class UnionSource {

    private List<Map<String, Object>> vicSource;
    private CapacitySource capacitySource;

    public List<Map<String, Object>> getVicSource() {
        return vicSource;
    }

    public void setVicSource(List<Map<String, Object>> vicSource) {
        this.vicSource = vicSource;
    }

    public CapacitySource getCapacitySource() {
        return capacitySource;
    }

    public void setCapacitySource(CapacitySource capacitySource) {
        this.capacitySource = capacitySource;
    }
}
