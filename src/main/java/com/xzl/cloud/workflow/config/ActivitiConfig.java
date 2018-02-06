package com.xzl.cloud.workflow.config;

import com.alibaba.druid.pool.DruidDataSource;
import org.activiti.engine.impl.cfg.IdGenerator;
import org.activiti.engine.impl.persistence.StrongUuidGenerator;
import org.activiti.spring.SpringAsyncExecutor;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.activiti.spring.boot.AbstractProcessEngineAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.io.IOException;

@Configuration
public class ActivitiConfig extends AbstractProcessEngineAutoConfiguration{
    @Resource
    DruidDataSource activitiDataSource;//注入配置好的数据源

    @Resource
    PlatformTransactionManager activitiTransactionManager;//注入配置好的事物管理器

    @Bean
    public IdGenerator idGenerator() {
        return new StrongUuidGenerator();
    }
    //注入数据源和事务管理器
    @Bean
    public SpringProcessEngineConfiguration springProcessEngineConfiguration(
            SpringAsyncExecutor springAsyncExecutor) throws IOException {
        SpringProcessEngineConfiguration spec = this.baseSpringProcessEngineConfiguration(activitiDataSource, activitiTransactionManager, springAsyncExecutor);
        spec.setIdGenerator(idGenerator());
        return spec;
    }
}
