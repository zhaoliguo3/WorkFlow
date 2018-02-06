package com.xzl.cloud.workflow.process;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
@Data
public class RollBack implements Serializable {
    private String user;
    private String advise;
    private Date createTime;
}
