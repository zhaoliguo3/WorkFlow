package com.xzl.cloud.workflow.service.impl;

import com.xzl.boilerplate.common.dto.Pager;
import com.xzl.boilerplate.common.dto.exception.BizCode;
import com.xzl.boilerplate.common.dto.exception.BizException;
import com.xzl.cloud.workflow.dto.TaskDTO;
import com.xzl.cloud.workflow.process.RollBack;
import com.xzl.cloud.workflow.process.TaskOrderMapper.TaskOrderMapper;
import com.xzl.cloud.workflow.service.CommonService;
import lombok.extern.java.Log;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.TransitionImpl;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.image.ProcessDiagramGenerator;
import org.activiti.spring.ProcessEngineFactoryBean;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Service("commonService")
@Log
public class CommonServiceImpl implements CommonService {
    //注入为我们自动配置好的服务
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
    @Autowired
    WeibaoServiceImpl weibaoService;

    //开始流程，参数放在map中传入
    public String startProcess(Map<String, Object> map, String process) {
        ProcessInstance processInstance = null;

        if (map == null)
            processInstance = runtimeService.startProcessInstanceByKey(process);
        else
            processInstance = runtimeService.startProcessInstanceByKey(process, map);

        return processInstance.getId();
    }

    //获得某个人的任务别表
    public Pager getTasks(String assignee, Integer index, Integer size) {
        Pager pagenation = new Pager(index, size);
        List<Task> tasks = new ArrayList<>();
//        tasks.addAll(taskService.createTaskQuery().taskCandidateUser(assignee).list());
        tasks.addAll(taskService.createTaskQuery().taskAssignee(assignee).listPage(pagenation.getStart(), pagenation.getEnd()));
        List<TaskDTO> dtos = new ArrayList<>();


        ProcessInstance processInstance=null;
        if (tasks.size() > 0) {
            for (Task task : tasks) {
                processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(task.getProcessInstanceId()).singleResult();
                dtos.add(new TaskDTO(task.getId(), task.getName(), task.getProcessInstanceId(), task.getTaskDefinitionKey(), processInstance.getBusinessKey(), task.getCreateTime()));
            }
        }
        pagenation.setList(dtos);
        pagenation.setTotal((int) taskService.createTaskQuery().taskAssignee(assignee).count());
        return pagenation;
    }

    //完成任务
    public void completeTasks(Map map, String taskId) throws BizException {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null)
            throw new BizException(BizCode.TASK_NOT_EXIST);
        if (map == null || map.isEmpty())
            taskService.complete(taskId);
        else
            taskService.complete(taskId, map);
    }

   /* public void rejectTask(String procInstId, String destTaskKey, String rejectMessage) {

        //获得当前任务的对应实列
        Task taskEntity = taskService.createTaskQuery().processInstanceId(procInstId).singleResult();
        //当前任务key
        String taskDefKey = taskEntity.getTaskDefinitionKey();
        //获得当前流程的定义模型
        ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                .getDeployedProcessDefinition(taskEntity.getProcessDefinitionId());

        //获得当前流程定义模型的所有任务节点
        List<ActivityImpl> activitilist = processDefinition.getActivities();
        //获得当前活动节点和驳回的目标节点"draft"
        ActivityImpl currActiviti = null;//当前活动节点
        ActivityImpl destActiviti = null;//驳回目标节点
        int sign = 0;
        for (ActivityImpl activityImpl : activitilist) {
            //确定当前活动activiti节点
            if (taskDefKey.equals(activityImpl.getId())) {
                currActiviti = activityImpl;

                sign++;
            } else if (destTaskKey.equals(activityImpl.getId())) {
                destActiviti = activityImpl;
                sign++;
            }
            //System.out.println("//-->activityImpl.getId():"+activityImpl.getId());
            if (sign == 2) {
                break;//如果两个节点都获得,退出跳出循环
            }
        }
       // System.out.println("//-->currActiviti activityImpl.getId():" + currActiviti.getId());
       // System.out.println("//-->destActiviti activityImpl.getId():" + destActiviti.getId());
        //保存当前活动节点的流程想参数
        List<PvmTransition> hisPvmTransitionList = new ArrayList<PvmTransition>(0);

        if (currActiviti!=null)
        for (PvmTransition pvmTransition : currActiviti.getOutgoingTransitions()) {
            hisPvmTransitionList.add(pvmTransition);
        }
        //清空当前活动节点的所有流出项
        currActiviti.getOutgoingTransitions().clear();
       // System.out.println("//-->currActiviti.getOutgoingTransitions().clear():" + currActiviti.getOutgoingTransitions().size());
        //为当前节点动态创建新的流出项
        TransitionImpl newTransitionImpl = currActiviti.createOutgoingTransition();
        //为当前活动节点新的流出目标指定流程目标
        newTransitionImpl.setDestination(destActiviti);
        //保存驳回意见
        taskEntity.setDescription(rejectMessage);//设置驳回意见
        taskService.saveTask(taskEntity);
        //设定驳回标志
        Map<String, Object> variables = new HashMap<String, Object>(0);
        variables.put(WfConstant.WF_VAR_IS_REJECTED.value(), WfConstant.IS_REJECTED.value());
        //执行当前任务驳回到目标任务draft
        taskService.complete(taskEntity.getId(), variables);
        //清除目标节点的新流入项
        if (destActiviti!=null)
        destActiviti.getIncomingTransitions().remove(newTransitionImpl);
        //清除原活动节点的临时流程项
        currActiviti.getOutgoingTransitions().clear();
        //还原原活动节点流出项参数
        currActiviti.getOutgoingTransitions().addAll(hisPvmTransitionList);
    }*/

    //回退到指定节点
    public boolean rollBackToDest(String taskId, String destTaskkey, RollBack rollBack) {
                Task exsit=taskService.createTaskQuery().taskId(taskId).singleResult();
                if (!(exsit!=null))
                    throw new  BizException(BizCode.TASK_NOT_EXIST);
       String worker= (String) taskService.getVariables(taskId).get("worker");
        try {
            Map<String, Object> variables;
            // 取得当前任务.当前任务节点
            HistoricTaskInstance currTask = historyService
                    .createHistoricTaskInstanceQuery().taskId(taskId)
                    .singleResult();
            // 取得流程实例，流程实例
            ProcessInstance instance = runtimeService
                    .createProcessInstanceQuery()
                    .processInstanceId(currTask.getProcessInstanceId())
                    .singleResult();
            if (instance == null) {
                log.info("流程结束");
                return false;
            }
            variables = taskService.getVariables(taskId);
            if (variables == null)
                variables = new HashMap<>();
            if (variables.get("rollBack") != null) {
                List<RollBack> rolls = (List) variables.get("rollBack");
                rolls.add(rollBack);
                variables.put("rollBack", rolls);
            } else {
                List<RollBack> rollBacks = new ArrayList<>();
                rollBacks.add(rollBack);
                variables.put("rollBack", rollBacks);
            }
            // 取得流程定义
            ProcessDefinitionEntity definition = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                    .getDeployedProcessDefinition(currTask
                            .getProcessDefinitionId());
            if (definition == null) {
                log.info("流程定义未找到");
                return false;
            }
            //取得当前活动节点
            ActivityImpl currActivity = (definition)
                    .findActivity(currTask.getTaskDefinitionKey());

            log.info("currActivity" + currActivity);
            // 取得上一步活动
            //也就是节点间的连线
            //获取来源节点的关系
            List<PvmTransition> inTransitionList = currActivity.getIncomingTransitions();
            // 清除当前活动的出口
            List<PvmTransition> oriPvmTransitionList = new ArrayList<>();
            //新建一个节点连线关系集合
            //获取出口节点的关系
            List<PvmTransition> outTransitionList = currActivity
                    .getOutgoingTransitions();
            //
            for (PvmTransition pvmTransition : outTransitionList) {
                oriPvmTransitionList.add(pvmTransition);
            }
            outTransitionList.clear();

            // 建立新出口
            List<TransitionImpl> newTransitions = new ArrayList<>();
            for (PvmTransition nextTransition : inTransitionList) {
                if (newTransitions.size() > 0)
                    break;
                PvmActivity nextActivity = nextTransition.getSource();

                log.info("nextActivity" + nextActivity);

                log.info("nextActivity.getId()" + nextActivity.getId());

                //destTaskkey
                ActivityImpl nextActivityImpl = (definition)
                        // .findActivity(nextActivity.getId());
                        .findActivity(destTaskkey);
                TransitionImpl newTransition = currActivity
                        .createOutgoingTransition();
                newTransition.setDestination(nextActivityImpl);
                newTransitions.add(newTransition);
            }
            // 完成任务
            List<Task> tasks = taskService.createTaskQuery()
                    .processInstanceId(instance.getId())
                    .taskDefinitionKey(currTask.getTaskDefinitionKey()).list();
            for (Task task : tasks) {
                taskService.complete(task.getId(), variables);
                historyService.deleteHistoricTaskInstance(task.getId());
            }
            // 恢复方向
            for (TransitionImpl transitionImpl : newTransitions) {
                currActivity.getOutgoingTransitions().remove(transitionImpl);
            }
            for (PvmTransition pvmTransition : oriPvmTransitionList) {
                outTransitionList.add(pvmTransition);
            }

            //weibaoService.claim(worker,instance.getId());


            log.info("OK");
            log.info("流程驳回结束");


            return true;
        } catch (Exception e) {
            log.info("失败");
            return false;
        }
    }

    @Override
    public byte[] generateImage(String processInstanceId, String taskId) throws IOException, BizException {
        if (processInstanceId == null) {
            Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
            if (task == null)
                throw new BizException(BizCode.TASK_NOT_EXIST);
            //processInstanceId
            processInstanceId = task.getProcessInstanceId();
        }

        //获取历史流程实例
        HistoricProcessInstance processInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();

        //获取流程图
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processInstance.getProcessDefinitionId());
        processEngineConfiguration = processEngine.getProcessEngineConfiguration();
        Context.setProcessEngineConfiguration((ProcessEngineConfigurationImpl) processEngineConfiguration);

        ProcessDiagramGenerator diagramGenerator = processEngineConfiguration.getProcessDiagramGenerator();
        ProcessDefinitionEntity definitionEntity = (ProcessDefinitionEntity) repositoryService.getProcessDefinition(processInstance.getProcessDefinitionId());

        List<HistoricActivityInstance> highLightedActivitList = historyService.createHistoricActivityInstanceQuery().processInstanceId(processInstanceId).list();
        //排除驳回
        //1 获取当前流程节点的序号order
        Task task = taskService.createTaskQuery().processInstanceId(processInstanceId).singleResult();
        Map<String, Integer> mapper = (Map) TaskOrderMapper.MAPPER().get(processInstance.getProcessDefinitionKey());
        int curOrder;
        if (task == null)
            curOrder = 100;
        else
            curOrder = mapper.get(task.getTaskDefinitionKey());
        List<HistoricTaskInstance> historicTaskInstances = historyService.createHistoricTaskInstanceQuery().processInstanceId(processInstanceId).list();
        Map<String, String> compareMap = new HashMap();
        for (HistoricTaskInstance historicTaskInstance : historicTaskInstances)
            compareMap.put(historicTaskInstance.getId(), historicTaskInstance.getTaskDefinitionKey());
        //2 遍历排除序号大于当前流程节点的历史任务
        Iterator iter = highLightedActivitList.iterator();
        Map keyMap = new HashMap(); //排序因驳回出现的多个相同节点
        while (iter.hasNext()) {
            HistoricActivityInstance activity = (HistoricActivityInstance) iter.next();
            if (keyMap.get(activity.getActivityId())!=null) {
                iter.remove();
            } else {
                keyMap.put(activity.getActivityId(), 1);
                if (activity.getEndTime() != null && mapper.get(activity.getActivityId()) > curOrder)
                    iter.remove();
            }
//            if (!compareMap.containsKey(activity.getTaskId())&&!activity.getActivityType().equals("startEvent"))
//                iter.remove();
        }

        //高亮环节id集合
        List<String> highLightedActivitis = new ArrayList<>();
        //高亮线路id集合
        List<String> highLightedFlows = getHighLightedFlows(definitionEntity, highLightedActivitList);

        for (HistoricActivityInstance tempActivity : highLightedActivitList) {
            String activityId = tempActivity.getActivityId();
            highLightedActivitis.add(activityId);
        }

        //设置字体就好了
        InputStream imageStream = diagramGenerator.generateDiagram(bpmnModel, "png", highLightedActivitis, highLightedFlows, "宋体", "宋体", "", null, 1.0);
        //单独返回流程图，不高亮显示
//        InputStream imageStream = diagramGenerator.generatePngDiagram(bpmnModel);
        // 输出资源内容到相应对象

        byte[] in2b = IOUtils.toByteArray(imageStream);
        return in2b;
    }

    @Override
    public Pager getHisTasks(String assigee, String procInsId, Integer index, Integer size) {
        Pager pagenation = new Pager(index, size);
        List<TaskDTO> dtos = new ArrayList<>();
        List<HistoricTaskInstance> list;
        if (procInsId == null) {
            list = historyService.createHistoricTaskInstanceQuery().finished().orderByHistoricTaskInstanceEndTime().asc().listPage(pagenation.getStart(), pagenation.getEnd());
            pagenation.setTotal((int) historyService.createHistoricTaskInstanceQuery().finished().count());
        } else {
            list = historyService.createHistoricTaskInstanceQuery().processInstanceId(procInsId).finished().orderByHistoricTaskInstanceEndTime().asc().listPage(pagenation.getStart(), pagenation.getEnd());
            pagenation.setTotal((int) historyService.createHistoricTaskInstanceQuery().processInstanceId(procInsId).finished().count());
        }


       HistoricProcessInstance historicProcessInstance= historyService.createHistoricProcessInstanceQuery().processInstanceId(procInsId).singleResult();
        for (HistoricTaskInstance hisTask : list) {
            dtos.add(new TaskDTO(hisTask.getId(), hisTask.getName(), hisTask.getProcessInstanceId(),hisTask.getTaskDefinitionKey(), historicProcessInstance.getBusinessKey(),hisTask.getStartTime(), hisTask.getEndTime()));
        }
        pagenation.setList(dtos);


        return pagenation;
    }

    /**
     * 获取需要高亮的线
     *
     * @param processDefinitionEntity
     * @param historicActivityInstances
     * @return
     */
    private List<String> getHighLightedFlows(
            ProcessDefinitionEntity processDefinitionEntity,
            List<HistoricActivityInstance> historicActivityInstances) {

        List<String> highFlows = new ArrayList<String>();// 用以保存高亮的线flowId
        for (int i = 0; i < historicActivityInstances.size() - 1; i++) {// 对历史流程节点进行遍历
            ActivityImpl activityImpl = processDefinitionEntity
                    .findActivity(historicActivityInstances.get(i)
                            .getActivityId());// 得到节点定义的详细信息
            List<ActivityImpl> sameStartTimeNodes = new ArrayList<ActivityImpl>();// 用以保存后需开始时间相同的节点
            ActivityImpl sameActivityImpl1 = processDefinitionEntity
                    .findActivity(historicActivityInstances.get(i + 1)
                            .getActivityId());
            // 将后面第一个节点放在时间相同节点的集合里
            sameStartTimeNodes.add(sameActivityImpl1);
            for (int j = i + 1; j < historicActivityInstances.size() - 1; j++) {
                HistoricActivityInstance activityImpl1 = historicActivityInstances
                        .get(j);// 后续第一个节点
                HistoricActivityInstance activityImpl2 = historicActivityInstances
                        .get(j + 1);// 后续第二个节点
                if (activityImpl1.getStartTime().equals(
                        activityImpl2.getStartTime())) {
                    // 如果第一个节点和第二个节点开始时间相同保存
                    ActivityImpl sameActivityImpl2 = processDefinitionEntity
                            .findActivity(activityImpl2.getActivityId());
                    sameStartTimeNodes.add(sameActivityImpl2);
                } else {
                    // 有不相同跳出循环
                    break;
                }
            }
            List<PvmTransition> pvmTransitions = activityImpl
                    .getOutgoingTransitions();// 取出节点的所有出去的线
            for (PvmTransition pvmTransition : pvmTransitions) {
                // 对所有的线进行遍历
                ActivityImpl pvmActivityImpl = (ActivityImpl) pvmTransition
                        .getDestination();
                // 如果取出的线的目标节点存在时间相同的节点里，保存该线的id，进行高亮显示
                if (sameStartTimeNodes.contains(pvmActivityImpl)) {
                    highFlows.add(pvmTransition.getId());
                }
            }
        }
        return highFlows;
    }





}
