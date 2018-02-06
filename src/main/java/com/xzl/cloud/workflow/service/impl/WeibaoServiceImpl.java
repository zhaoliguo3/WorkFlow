package com.xzl.cloud.workflow.service.impl;

import com.xzl.boilerplate.common.dto.ResultResponse;
import com.xzl.boilerplate.common.dto.exception.BizCode;
import com.xzl.boilerplate.common.dto.exception.BizException;
import com.xzl.boilerplate.common.utils.RestTemplateUtil;
import com.xzl.cloud.workflow.dto.TaskDTO;
import com.xzl.cloud.workflow.process.RollBack;
import com.xzl.cloud.workflow.process.TaskOrderMapper.TaskOrderMapper;
import com.xzl.cloud.workflow.service.WeibaoService;
import org.activiti.engine.*;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.spring.ProcessEngineFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("weibaoService")
public class WeibaoServiceImpl implements WeibaoService {


    public static final String procKey = "order";

    @Autowired
    CommonServiceImpl commonService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    HistoryService historyService;
    @Autowired
    RepositoryService repositoryService;
    @Autowired
    ProcessEngineConfiguration processEngineConfiguration;
    @Autowired
    ProcessEngineFactoryBean processEngine;

    DelegateExecution execution;

    @Override
    public boolean rollBackToWeibaoForm(String taskId, RollBack rollBack) {
        return commonService.rollBackToDest(taskId,"weibaoForm",rollBack);
    }

    @Override
    public boolean rollBackToBegin(String procInsId, RollBack rollBack) {
       String taskId= getRunTask(procInsId).getId();
        return commonService.rollBackToDest(taskId,"worker",rollBack);
    }

    @Override
    public String startProcess(Map<String, Object> map, String process, String businessKey) throws MalformedURLException {
        ProcessInstance processInstance = null;
        String proInsId=null;
        if(businessKey==null) {
             proInsId = commonService.startProcess(map,process);
        }else {
            if (map == null)
                processInstance = runtimeService.startProcessInstanceByKey(process,businessKey);
            else {
                processInstance = runtimeService.startProcessInstanceByKey(process, businessKey, map);
            }
             proInsId=processInstance.getId();
        }

        return proInsId;
    }


    @Override
    public Map getStatus(String procInsId) {
         List<Task> tasks= taskService.createTaskQuery().processInstanceId(procInsId).list();
         if (tasks!=null&&tasks.size()>0)
            return getMap(procInsId);
            else return null;
    }

    @Override
    public TaskDTO getRunTask(String procInsId){
        ProcessInstance processInstance=runtimeService.createProcessInstanceQuery().processInstanceId(procInsId).processDefinitionKey(procKey).singleResult();
        if (processInstance!=null) {
            String id = processInstance.getActivityId();
            Map map=(Map)TaskOrderMapper.MAPPER().get("weibaoWorker");
            Task task = taskService.createTaskQuery().taskDefinitionKey(id).processInstanceId(procInsId).processDefinitionKey(procKey).singleResult();
            if (task!=null)
            return new TaskDTO(task.getId(), task.getName(), procInsId, String.valueOf(map.get(id)) , processInstance.getBusinessKey(), task.getCreateTime());
            else return null;
        }
        else return null;
    }

    @Override
    public Map forciblyEnd(Map map,String procInsId) {
        if(this.getStatus(procInsId)!=null) {
        completeTasks(map,this.getRunTask(procInsId).getId());
            return getMap(procInsId);
        }
        else return null;

    }


    @Override
    public List getVariables(String id, int code) {
        //判断是否还有流程正在执行
        Boolean noEnd=false;
        List<ProcessInstance> processInstances= runtimeService.createProcessInstanceQuery().processDefinitionKey(procKey).list();
        if (processInstances.size()>0)
            for (ProcessInstance processInstance:processInstances)
                if (getRunTask(processInstance.getId())!=null)
                    noEnd=true;

        List list=new ArrayList();
        if (code>0&&noEnd){

            for (ProcessInstance processInstance:processInstances)
            {
                if (getStatus(processInstance.getId())!=null){
                Map map=runtimeService.getVariables(processInstance.getId());
                if(map!=null&&map.size()>0) {
                    for (Object value : map.values()) {
                        if (id.equals(value))
                            list.add(getMap(processInstance.getId())); } } } }
                                          return list;
        }

        if (code<=0&&!noEnd){
            List<HistoricProcessInstance> historicProcessInstances=historyService.createHistoricProcessInstanceQuery().list();
            for (HistoricProcessInstance historicProcessInstance:historicProcessInstances) {
                      List<HistoricVariableInstance> list1=  historyService.createHistoricVariableInstanceQuery().processInstanceId(historicProcessInstance.getId()).list();
                Map map = new HashMap();
                 if (list1!=null&&list1.size()>0)
                 {
                     for (HistoricVariableInstance historicVariableInstance  : list1) {
                         if (id.equals(historicVariableInstance.getValue())) {
                               for (HistoricVariableInstance historicVariableInstance1  : list1){
                                   map.put(historicVariableInstance1.getVariableName(),historicVariableInstance1.getValue());
                               }
                               map.put("businessKey",historicProcessInstance.getBusinessKey());
                               map.put("procInsId",historicProcessInstance.getId());
                               list.add(map);

                         }
                     }
                 }

            }
            return list;

        }
             return null;

    }


    @Override
    public void completeTasks(Map map, String taskId) throws BizException {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        String procInsId = task.getProcessInstanceId();
        Map variable = runtimeService.getVariables(procInsId);

        String worker = (String) variable.get("worker");
        if (map == null) {
            if (task.getAssignee().equals(worker))
                commonService.completeTasks(map, taskId);
            else throw new BizException(BizCode.REQUEST_PARAM_ERROR);
        } else {
            String messages = (String) map.get("message");
            if (messages == null || task.getAssignee().equals(worker))
                throw new BizException(BizCode.REQUEST_PARAM_ERROR);


            if (!messages.equals("审核通过") && !messages.equals("审核不通过"))
                throw new BizException(BizCode.REQUEST_PARAM_ERROR);

            commonService.completeTasks(map, taskId);

        }

    }

    @Override
    public Map getMap(String procInsId)  {
        ProcessInstance processInstance=runtimeService.createProcessInstanceQuery().processInstanceId(procInsId).processDefinitionKey(procKey).singleResult();
        if (processInstance!=null){
            Map map = runtimeService.getVariables(procInsId);
            Map map1=(Map)TaskOrderMapper.MAPPER().get("weibaoWorker");
            map.put("businessKey", processInstance.getBusinessKey());
            map.put("taskKey", map1.get(processInstance.getActivityId()));
            map.put("proInsId", processInstance.getId());
            return map;}
        else return null;
    }


    public String getMaintenance(DelegateExecution execution) throws MalformedURLException {
  String url = "http://172.18.20.202:8230/order//web/v1/maint/getMaint?bizKey=1";
        RestTemplate restTemplate=new RestTemplate();
        Map body = new HashMap();
        String token = "eyJhbGciOiJSUzUxMiJ9.eyJndWlkIjoiNDQyIiwiZXhwIjoxNTIzNTYyODgwLCJzdWIiOiJjY2MiLCJjcmVhdGVkIjoxNTE2OTU4MDgwNDc0LCJybHMiOlt7ImF1dGhvcml0eSI6IldYQl9LRlBUIn1dfQ.OXgZXtwsgGfi3wvnRrpxcPIjO5AtRtcpZ9IDEn04S7z_Uyw9wZ22zXxf8_YfN0GFcFvEo2YsIUeo0zUafczdzkL6eNQcjPEocytsm1sekFyJlGh9_SVIq58GVMq3y1wabJdIewMtUgcNItr-jPpEHm7zlZwiSspbJilfC_TU_Lr4KJhJqdfpd7XFqFTBG9QabqvpgPrBIRl53vYkufUqKVdUchMivi5np4NXZmbG9U0wu_wgvxRZ0SB2-1GIdiFERf1Yp6QSASQAZ1hHuhgFdK3-y-Zin9C-SoDflELdNRyCd0G1tM_Sb5mA61gbR1zBUU8IJd4G8cu-x_WaRuqeeg";
        ResultResponse result = RestTemplateUtil.getResultReponse(restTemplate,url, HttpMethod.GET,body, token);
        String maintenance=(String)result.getData();
        execution.setVariable("maintenance",maintenance);
        return maintenance;
    }


    public String getProperty(DelegateExecution execution) throws MalformedURLException {
        String url = "http://172.18.20.202:8230/order//web/v1/maint/getProperty?bizKey=1";
        RestTemplate restTemplate=new RestTemplate();
        Map body = new HashMap();
        String token = "eyJhbGciOiJSUzUxMiJ9.eyJndWlkIjoiNDQyIiwiZXhwIjoxNTIzNTYyODgwLCJzdWIiOiJjY2MiLCJjcmVhdGVkIjoxNTE2OTU4MDgwNDc0LCJybHMiOlt7ImF1dGhvcml0eSI6IldYQl9LRlBUIn1dfQ.OXgZXtwsgGfi3wvnRrpxcPIjO5AtRtcpZ9IDEn04S7z_Uyw9wZ22zXxf8_YfN0GFcFvEo2YsIUeo0zUafczdzkL6eNQcjPEocytsm1sekFyJlGh9_SVIq58GVMq3y1wabJdIewMtUgcNItr-jPpEHm7zlZwiSspbJilfC_TU_Lr4KJhJqdfpd7XFqFTBG9QabqvpgPrBIRl53vYkufUqKVdUchMivi5np4NXZmbG9U0wu_wgvxRZ0SB2-1GIdiFERf1Yp6QSASQAZ1hHuhgFdK3-y-Zin9C-SoDflELdNRyCd0G1tM_Sb5mA61gbR1zBUU8IJd4G8cu-x_WaRuqeeg";
        ResultResponse result = RestTemplateUtil.getResultReponse(restTemplate,url, HttpMethod.GET,body, token);
        String property=(String)result.getData();
        execution.setVariable("property",property);
        return property;
    }

    /*    @Override
    public List getGroupTask(String assignee) {
        List<Task> tasks=  taskService.createTaskQuery().taskCandidateUser(assignee).processDefinitionKey(procKey).list();
        List<TaskDTO> list=new ArrayList<>();
        for(Task task:tasks){
            ProcessInstance processInstance=runtimeService.createProcessInstanceQuery().processDefinitionKey(procKey).processInstanceId(task.getProcessInstanceId()).singleResult();
            list.add(new TaskDTO(task.getId(),task.getName(),task.getProcessInstanceId(),task.getTaskDefinitionKey(),processInstance.getBusinessKey(),task.getCreateTime()));
    }
        return list;
    }

    @Override
    public synchronized Map claim(String assignee,String procInsId) {
        if (getGroupTask(assignee)!=null&&getGroupTask(assignee).size()>0){
        List<TaskDTO> tasks=this.getGroupTask(assignee);
        for (TaskDTO task:tasks){
            if (task.getProcessInsId().equals(procInsId)){
                taskService.claim(task.getId(),assignee);
                ////////////////////////////////////////////////////////////////////////////////////////////////////////
                taskService.setVariable(task.getId(),task.getKey(),assignee);
                Map map= taskService.getVariables(task.getId());
                List<String> workers= (List) map.get("workers");
            for (String worker:workers) {
                if (!assignee.equals(worker)){
                    taskService.setVariable(task.getId(),"confirmor",worker);
                }
            }
            }
        }

        return getMap(procInsId);}
        else return null;
    }*/


}
