package com.xzl.cloud.workflow.controller;

import com.xzl.boilerplate.common.dto.Pager;
import com.xzl.boilerplate.common.dto.ResultResponse;
import com.xzl.boilerplate.common.dto.exception.BizCode;
import com.xzl.boilerplate.common.dto.exception.BizException;
import com.xzl.cloud.workflow.dto.TaskDTO;
import com.xzl.cloud.workflow.process.RollBack;
import com.xzl.cloud.workflow.process.WeibaoWorkerUsers;
import com.xzl.cloud.workflow.process.WorkFlowRequest;
import com.xzl.cloud.workflow.service.CommonService;
import com.xzl.cloud.workflow.service.WeibaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "${wf.version}/weibao")
public class WeibaoController {
    @Autowired
    WeibaoService weibaoService;

    @Autowired
    CommonService commonService;


    /**
     * 驳回
     * @param map taskId 任务节点ID, advise驳回建议
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/rollBack", method = RequestMethod.POST)
    public ResultResponse rollBack(@RequestBody Map<String,String> map) throws IOException {
        RollBack rollBack = new RollBack();
        rollBack.setUser(map.get("user"));
        rollBack.setAdvise(map.get("advise"));
        rollBack.setCreateTime(new Date());
        boolean flag = weibaoService.rollBackToBegin(map.get("procInsId"), rollBack);
        if (flag)
            return ResultResponse.createSuccessResponse("ok");
        else
            return ResultResponse.createFailureResponse(BizCode.ROLL_BACK_FAIL);
    }

    /**
     * 启动流程
     * @param request  userSetup  proKey
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/start", method = RequestMethod.POST)
    public ResultResponse startProcessInstance(@RequestBody WorkFlowRequest<WeibaoWorkerUsers> request) throws IOException {

        Map map = request.getUserSetup().toMap();
        if (request.getUserSetup()==null)
            throw new BizException(BizCode.REQUEST_PARAM_ERROR);

             String worker= (String)map.get("worker");
            if (worker==null)
            throw new BizException(BizCode.REQUEST_PARAM_ERROR);


        String procInsId=weibaoService.startProcess(map, request.getProcKey(),request.getBusinessKey());


        return ResultResponse.createSuccessResponse(weibaoService.getMap(procInsId));
    }



    /**
     * 获取当前人的任务
     * @param assignee
     * @param index
     * @param size
     * @return
     */
    @RequestMapping(value = "/tasks", method = RequestMethod.GET)
    public ResultResponse<List<TaskDTO>> getTasks(@RequestParam String assignee, @RequestParam(defaultValue = "0") Integer index, @RequestParam(defaultValue = "1") Integer size) {
        Pager pagenation = commonService.getTasks(assignee,index,size);
        return ResultResponse.createSuccessResponse(pagenation);
    }


    /**
     * 完成任务
     * @param request
     * @return
     */
    @RequestMapping(value = "/complete", method = RequestMethod.POST)
    public ResultResponse complete(@RequestBody WorkFlowRequest<WeibaoWorkerUsers> request) {
        Map map=request.getProcessVar();
        weibaoService.completeTasks(map,request.getTaskId());
        return ResultResponse.createSuccessResponse("ok");
    }

    /**
     * 查询正在执行的任务
     * @param procInsId
     * @return
     */
    @RequestMapping(value = "/runTask", method = RequestMethod.GET)
    public ResultResponse<TaskDTO> getRunTask(@RequestParam String procInsId) {
            return ResultResponse.createSuccessResponse(weibaoService.getRunTask(procInsId));
    }

    /**
     * 查询流程状态
     * @param procInsId
     * @return
     */
    @RequestMapping(value = "/getStatus", method = RequestMethod.GET)
    public ResultResponse getStatus(@RequestParam String procInsId) {
       return ResultResponse.createSuccessResponse(weibaoService.getStatus(procInsId));
    }

    /**
     * 完成当前流程正在执行的任务
     * @param request
     * @return
     */
    @RequestMapping(value = "/forciblyEnd", method = RequestMethod.POST)
    public ResultResponse forciblyEnd(@RequestBody WorkFlowRequest<WeibaoWorkerUsers> request) {
        return ResultResponse.createSuccessResponse( weibaoService.forciblyEnd(request.getProcessVar(),request.getProcInsId()));
    }

    /**
     * 获取流程变量
     * @param id
     * @param code
     * @return
     */
    @RequestMapping(value = "/variables", method = RequestMethod.GET)
    public ResultResponse getVariables(@RequestParam String id,@RequestParam int code) {
        return ResultResponse.createSuccessResponse(weibaoService.getVariables(id,code));
    }

}
/*    *//**
 * 获取组任务
 * @param assignee
 * @return
 *//*
    @RequestMapping(value = "/getGroupTask", method = RequestMethod.GET)
    public ResultResponse getGroupTask(@RequestParam String assignee) {
        return ResultResponse.createSuccessResponse(weibaoService.getGroupTask(assignee));
    }

    *//**
 * 签到拾取任务
 * @param
 * @return
 *//*
    @RequestMapping(value = "/claim", method = RequestMethod.POST)
    public ResultResponse claim(@RequestBody WorkFlowRequest<WeibaoWorkerUsers> request){
        return ResultResponse.createSuccessResponse(weibaoService.claim(request.getAssignee(),request.getProcInsId()));
    }*/
