package com.cn.ridge.bean;

import java.util.List;

/**
 * Author: create by wang.gf
 * Date: create at 2018/11/15
 */
public class CiObject {
    private Long ciId;
    private String ciName;
    private String ciLabel;
    private List<KpiObject> kpis;
    private String ciSource;//用来区分数据来源于全量ci还是相关ci

    public String getCiSource() {
        return ciSource;
    }

    public void setCiSource(String ciSource) {
        this.ciSource = ciSource;
    }

    public String getCiLabel() {
        return ciLabel;
    }

    public void setCiLabel(String ciLabel) {
        this.ciLabel = ciLabel;
    }

    public Long getCiId() {
        return ciId;
    }

    public void setCiId(Long ciId) {
        this.ciId = ciId;
    }

    public String getCiName() {
        return ciName;
    }

    public void setCiName(String ciName) {
        this.ciName = ciName;
    }

    public List<KpiObject> getKpis() {
        return kpis;
    }

    public void setKpis(List<KpiObject> kpis) {
        this.kpis = kpis;
    }
}
