package com.xzl.cloud.workflow.service;

import com.xzl.boilerplate.common.dto.Pager;
import com.xzl.boilerplate.common.dto.exception.BizException;
import com.xzl.cloud.workflow.process.RollBack;

import java.io.IOException;
import java.util.Map;

public interface CommonService {
    /**
     * 启动一个流程
     * @param map 流程参数（表单）
     * @param process 流程id
     */
    String startProcess(Map<String, Object> map, String process);

    /**
     * 获取分配给assignee的任务
     * @param assignee
     * @return
     */
    Pager getTasks(String assignee, Integer index, Integer size);

    /**
     * 完成任务
     * @param map 流程参数
     * @param taskId 任务id
     * @throws BizException
     */
    void completeTasks(Map map, String taskId)throws BizException;

    /**
     * 流程状态图
     * @param processInsId 流程实例id
     * @param taskId
     * @return
     * @throws IOException
     */
    byte[] generateImage(String processInsId,String taskId) throws IOException;

    /**
     * 查询用户在某流程中已经完成的任务
     * @param assigee
     * @param procInsId
     * @return
     */
    Pager getHisTasks(String assigee, String procInsId, Integer index, Integer size);

    /**
     * 驳回
     * @param procInstId
     * @param destTaskKey
     * @param rejectMessage
     */
    //void rejectTask(String procInstId, String destTaskKey, String rejectMessage);

    /**
     * 回退到指定节点
     * @param taskId 任务节点ID
     * @param destTaskkey 目标节点定义Key
     * @return
     */
    boolean rollBackToDest(String taskId,String destTaskkey,RollBack rollBack);


}
