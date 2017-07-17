package com.ethan.apigatewaytry.dto;

public class ResResult {

    private String code;
    private Object detail;

    public ResResult(String code, Object detail) {
        this.code = code;
        this.detail = detail;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Object getDetail() {
        return detail;
    }

    public void setDetail(Object detail) {
        this.detail = detail;
    }

    @Override
    public String toString() {
        return "ResResult{" +
                "code='" + code + '\'' +
                ", detail=" + detail +
                '}';
    }

}
