package com.xzl.cloud.workflow.service;

import com.xzl.boilerplate.common.dto.exception.BizException;
import com.xzl.cloud.workflow.dto.TaskDTO;
import com.xzl.cloud.workflow.process.RollBack;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;

public interface WeibaoService {

    /**
     * 回退到维保工单节点
     * @param taskId 任务节点ID
     * @return
     */
    boolean rollBackToWeibaoForm(String taskId,RollBack rollBack);

    boolean rollBackToBegin(String procInsId, RollBack rollBack);

    /**
     * 启动一个流程
     * @param map 流程参数（表单）
     * @param procKey 流程id
     */
    String startProcess(Map<String, Object> map, String procKey, String businessKey) throws MalformedURLException;


    /**
     * 完成个人任务
     * @param map
     * @param taskId
     * @throws BizException
     */
    void completeTasks(Map map, String taskId) throws BizException;



    /**
     * 获取流程状态是否结束
     * @param procInsId
     * @return
     */
    Map getStatus(String procInsId);

    /**
     * 查询正在执行的任务
     * @param procInsId
     * @return
     */
    TaskDTO getRunTask(String procInsId);

    /**
     * 完成当前任务
     * @param procInsId
     * @return
     */
    Map forciblyEnd (Map map,String procInsId);

    /**
     * 获取流程变量
     * @param id
     * @param code
     * @return
     */
    List getVariables(String id,int code);


    Map getMap(String procInsId) throws MalformedURLException;



    /**
     * 查询当前人的组任务
     * @param assignee
     * @return
     */
    // List getGroupTask(String assignee);


    /**
     * 拾取组任务
     * @param assignee  指定办理人
     */
    //  Map claim(String assignee,String procInsId);



}
