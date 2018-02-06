package com.xzl.cloud.workflow.process;

import lombok.Data;

import java.util.Map;

@Data
public abstract class ProcRequest<T> {
    T userSetup;
    Map processVar;
    String taskId;
    String procInsId;
    String assignee;
    String procKey;

    String businessKey;
}
