package com.xzl.cloud.workflow.dto;

public enum  WfConstant {
    WF_VAR_IS_REJECTED("reject"),IS_REJECTED("true");

    private String value;
    WfConstant(String value) {
        this.value=value;
    }

    public String value() {
        return value;
    }
}
