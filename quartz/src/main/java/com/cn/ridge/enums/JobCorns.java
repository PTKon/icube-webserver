package com.cn.ridge.enums;

public enum JobCorns {
    hour("0 %d * * * ? *"),
    day("0 %d %d * * ? *"),
    //week 1-7，周日是1
    week("0 %d %d ? * %d"),//s m h d m w y
    month("0 %d %d %d * ?"),
    month_last_day("0 %d %d L * ?"),
    year("0 %d %d %d %d ? %s"),//每年固定月-天-日-小时-分执行
    once("0 %d %d %d %d ? %d"),
    year_month_last_day("0 %d %d L %d ? %s");//每年固定月-最后一天-日-小时-分执行

    private String cronTemplate;

    JobCorns(String cronTemplate) {
        this.cronTemplate = cronTemplate;
    }

    public String getCronTemplate() {
        return cronTemplate;
    }
}
