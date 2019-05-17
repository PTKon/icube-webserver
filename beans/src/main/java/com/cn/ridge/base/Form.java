package com.cn.ridge.base;

import com.cn.ridge.enums.ReturnCode;

/**
 * Author: create by wang.gf
 * Date: create at 2018/11/19
 */
public class Form<T> {
    private String code;

    private String msg;

    private T data;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Form(ReturnCode code, String msg) {
        this.code = code.name();
        this.msg = msg;
    }

    public Form(ReturnCode code, String msg, T data) {
        this.code = code.name();
        this.msg = msg;
        this.data = data;
    }

    public Form() {
    }
}
