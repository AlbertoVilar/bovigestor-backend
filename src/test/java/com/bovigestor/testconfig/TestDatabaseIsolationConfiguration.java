package com.bovigestor.testconfig;

import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration(proxyBeanMethods = false)
class TestDatabaseIsolationConfiguration {

    @Bean
    static BeanFactoryPostProcessor testDatabaseIsolationGuard(Environment environment) {
        return beanFactory -> TestDatabaseIsolationPolicy.validate(
                environment,
                beanFactory.containsBeanDefinition("dataSource")
        );
    }
}
