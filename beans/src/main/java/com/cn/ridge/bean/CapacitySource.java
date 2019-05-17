package com.cn.ridge.bean;

import java.util.List;
import java.util.Map;

/**
 * Author: create by wang.gf
 * Date: create at 2018/12/26
 */
public class CapacitySource {
    private List<Map<String, Object>> upperBoundaryLine;
    private List<Map<String, Object>> lowerBoundaryLine;
    private List<Map<String, Object>> capacityResult;

    public List<Map<String, Object>> getUpperBoundaryLine() {
        return upperBoundaryLine;
    }

    public void setUpperBoundaryLine(List<Map<String, Object>> upperBoundaryLine) {
        this.upperBoundaryLine = upperBoundaryLine;
    }

    public List<Map<String, Object>> getLowerBoundaryLine() {
        return lowerBoundaryLine;
    }

    public void setLowerBoundaryLine(List<Map<String, Object>> lowerBoundaryLine) {
        this.lowerBoundaryLine = lowerBoundaryLine;
    }

    public List<Map<String, Object>> getCapacityResult() {
        return capacityResult;
    }

    public void setCapacityResult(List<Map<String, Object>> capacityResult) {
        this.capacityResult = capacityResult;
    }
}
