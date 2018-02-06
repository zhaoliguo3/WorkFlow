package com.xzl.cloud.workflow;

import com.xzl.cloud.workflow.domain.mapper.UserMapper;
import com.xzl.cloud.workflow.domain.modul.User;
import com.xzl.cloud.workflow.service.LadderProductionService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = WorkFlowServiceApplication.class)
public class LadderProductionTest {
    @Autowired
    LadderProductionService ladderProductionService;

    @Autowired
    RuntimeService runtimeService;
    @Autowired
    TaskService taskService;
    @Test
    public void testGetTasks() {
//        List<TaskDTO> list = ladderProductionService.getTasks("jack-boc",0,10);
//        for (TaskDTO taskDTO : list)
//            System.out.println(taskDTO.toString());
    }

    @Test
    public void testComplete() {

    }

//    @Test
//    public void testAllHisTasks() {
//        List<TaskDTO> list = ladderProductionService.getHisTasks("jack-boc", null);
//        for (TaskDTO taskDTO : list)
//            System.out.println(taskDTO.toString());
//    }
//
//    @Test
//    public void testGetTasksByProInsId() {
//        List<TaskDTO> list = ladderProductionService.getHisTasks("jack-boc", "5");
//        for (TaskDTO taskDTO : list)
//            System.out.println(taskDTO.toString());
//    }

    @Test
    public void testStartProcess() {
        List categories = new ArrayList();
        categories.add(1);
        categories.add(0);
        Map map = new HashMap();
        map.put("user", "jack-boc");
        map.put("categories", categories);
        ladderProductionService.startProcess(map,"ladderProduction");
    }

    @Test
    public void reject() {
        ladderProductionService.rollBackToDest("489ae6bc-e568-11e7-8c53-94c6910dd3b1", "_4",null);
    }

    /**
     * 接口测试
     */
    private TestRestTemplate testRestTemplate;
    private URL base;
    @Value("${spring.application.port}")
    String port;
//    @Autowired
//    ActivitiDataSourceProperties activitiDataSourceProperties;

    @Before
    public void setTestRestTemplate() {
        testRestTemplate = new TestRestTemplate();
    }

    @Before
    public void setUp() throws Exception {
        this.base = new URL("http://localhost:" + port + "/hi?name=jay");
    }

    @Test
    public void getHello2() throws Exception {
        ResponseEntity<String> response = testRestTemplate.getForEntity(base.toString(),
                String.class);
        assertThat(response.getBody(), equalTo("hi jay,i am from port:8762"));
    }

    @Autowired
    UserMapper userMapper;

    @Test
    public void testMybatis() {
        User user = new User();
        user.setName("hahaha");
        userMapper.insert(user);
        int a=1;
    }



}

