package com.xzl.cloud.workflow;

import org.activiti.engine.HistoryService;
import org.activiti.engine.history.HistoricVariableInstance;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class WorkFlowServiceApplicationTests {
	@Autowired
	HistoryService historyService;



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



	@Test
	public void contextLoads() {
		Map map = new HashMap();
		List<HistoricVariableInstance> list =historyService.createHistoricVariableInstanceQuery().processInstanceId("316eafa7-02ba-11e8-98db-303a6467617d").list();
		historyService.createHistoricProcessInstanceQuery().processInstanceId("316eafa7-02ba-11e8-98db-303a6467617d").singleResult().getBusinessKey();
		for (HistoricVariableInstance historicVariableInstance : list) {
			historicVariableInstance.getValue();
			map.put(historicVariableInstance.getVariableName(), historicVariableInstance.getValue());
		}

	}

}
