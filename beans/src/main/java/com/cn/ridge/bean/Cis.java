package com.cn.ridge.bean;

import java.util.List;

/**
 * Author: create by wang.gf
 * Date: create at 2019/1/8
 */
public class Cis {
    private List<CiObject> selfCis;
    private List<CiObject> otherCis;
    private String ciLabels;
    private String kpiLabels;

    public List<CiObject> getSelfCis() {
        return selfCis;
    }

    public void setSelfCis(List<CiObject> selfCis) {
        this.selfCis = selfCis;
    }

    public List<CiObject> getOtherCis() {
        return otherCis;
    }

    public void setOtherCis(List<CiObject> otherCis) {
        this.otherCis = otherCis;
    }

    public String getCiLabels() {
        return ciLabels;
    }

    public void setCiLabels(String ciLabels) {
        this.ciLabels = ciLabels;
    }

    public String getKpiLabels() {
        return kpiLabels;
    }

    public void setKpiLabels(String kpiLabels) {
        this.kpiLabels = kpiLabels;
    }
}
