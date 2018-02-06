package com.xzl.cloud.workflow.controller;

import com.xzl.boilerplate.common.dto.Pager;
import com.xzl.boilerplate.common.dto.ResultResponse;
import com.xzl.boilerplate.common.dto.exception.BizCode;
import com.xzl.boilerplate.common.dto.exception.BizException;
import com.xzl.cloud.workflow.dto.TaskDTO;
import com.xzl.cloud.workflow.process.UserSetup;
import com.xzl.cloud.workflow.process.WorkFlowRequest;
import com.xzl.cloud.workflow.service.CommonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "${wf.version}/common")
public class WorkFlowController {
    @Autowired
    CommonService commonService;

    /**
     * 启动流程
     * @param query  userSetup  proKey
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/start", method = RequestMethod.POST)
    public ResultResponse startProcessInstance(@RequestBody WorkFlowRequest<UserSetup> query) throws IOException {
        Map map = query.getUserSetup().toMap();
        String proInsId = commonService.startProcess(map, query.getProcKey());
        return ResultResponse.createSuccessResponse(proInsId);
    }

    //获取当前人的任务
    @RequestMapping(value = "/tasks", method = RequestMethod.GET)
    public ResultResponse<List<TaskDTO>> getTasks(@RequestParam String assignee,@RequestParam(defaultValue = "0") Integer index, @RequestParam(defaultValue = "1") Integer size) {
        Pager pagenation = commonService.getTasks(assignee,index,size);
        return ResultResponse.createSuccessResponse(pagenation);
    }

    @RequestMapping(value = "/hisTasks",method =RequestMethod.GET)
    public ResultResponse<List<TaskDTO>> getHisTasks(@RequestParam String assignee,@RequestParam(required = false)String procInsId,
                                                     @RequestParam(defaultValue = "0") Integer index, @RequestParam(defaultValue = "1") Integer size) {
        Pager pagenation = commonService.getHisTasks(assignee,procInsId,index,size);
        return ResultResponse.createSuccessResponse(pagenation);
    }

    //完成任务
    @RequestMapping(value = "/complete", method = RequestMethod.POST)
    public ResultResponse complete(@RequestBody WorkFlowRequest<UserSetup> query) {
        commonService.completeTasks(query.getUserSetup().toMap(), query.getTaskId());
        return ResultResponse.createSuccessResponse("ok");
    }

    @RequestMapping(value = "/imgStatus")
    public void status(HttpServletResponse response,@RequestParam(required = false) String procInsId, @RequestParam(required = false) String taskId) throws IOException {
        if (procInsId == null && taskId == null)
            throw new BizException(BizCode.REQUEST_PARAM_ERROR);

        byte[] bytes = commonService.generateImage(procInsId,taskId);
        InputStream imageStream = new ByteArrayInputStream(bytes);
        //输出资源内容到相应对象
        byte[] b = new byte[1024];
        int len;
        while ((len = imageStream.read(b, 0, 1024)) != -1) {
            response.getOutputStream().write(b, 0, len);
        }
    }



}
