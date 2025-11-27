package com.deloitte.employee.infra.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Getter
@Setter
@Configuration
@ConfigurationProperties("spring.jpa")
public class JpaConfigProperties {

    private String databasePlatform;
    private boolean showSql;
    private HibernateProperties hibernate;
    private Map<String, Object> properties;

    @Getter
    @Setter
    public static class HibernateProperties {
        private String ddlAuto;
    }
}
