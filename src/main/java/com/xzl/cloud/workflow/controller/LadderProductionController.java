package com.xzl.cloud.workflow.controller;

import com.xzl.cloud.workflow.service.LadderProductionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "${wf.version}/ladder")
public class LadderProductionController{

    @Autowired
    private LadderProductionService ladderProductionService;

}
