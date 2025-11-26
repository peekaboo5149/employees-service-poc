package com.deloitte.employee.infra.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;

import java.io.Serializable;
import javax.sql.DataSource;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * data source config class.
 */

@Setter
@Slf4j
@Getter
@Configuration
@ConfigurationProperties("spring.datasource")
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "com.deloitte.employee.infra.repositories", // correct package
        entityManagerFactoryRef = "employeeManagementEntityManagerFactoryBean",
        transactionManagerRef = "transactionManager"
)

@Primary
public class DataSourceConfig implements Serializable {
    private String url;
    private String username;
    private String password;
    private boolean readOnly;
    private long maxLifeTime;
    private long idleTimeOut;
    private int maximumPoolSize;
    private String driverClassName;
    private long connectionTimeout;
    private int minimumIdle;
    private int leakDetectionThreshold;
    private String connectionTestQuery;

    /**
     * Data source for Employee management db.
     *
     * @return DataSource: Employee Management DB DataSource.
     */
    @Bean(name = "employeeManagementDataSource")
    public DataSource getemployeeManagementDataSource() {
        log.info("Url {}", url);
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(url);
        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);
        hikariConfig.setReadOnly(readOnly);
        hikariConfig.setMaxLifetime(maxLifeTime);
        hikariConfig.setIdleTimeout(idleTimeOut);
        hikariConfig.setMinimumIdle(minimumIdle);
        hikariConfig.setMaximumPoolSize(maximumPoolSize);
        hikariConfig.setDriverClassName(driverClassName);
        hikariConfig.setConnectionTimeout(connectionTimeout);
        hikariConfig.setLeakDetectionThreshold(leakDetectionThreshold);
        hikariConfig.setConnectionTestQuery(connectionTestQuery);
        return new HikariDataSource(hikariConfig);
    }

    /**
     * Entity Manager Factory for Employee management db.
     *
     * @return LocalContainerEntityManagerFactoryBean: Employee Management EntityManagerFactory.
     */
    @Bean(name = "employeeManagementEntityManagerFactoryBean")
    public LocalContainerEntityManagerFactoryBean getemployeeManagementEntityManagerFactory() {
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setGenerateDdl(false);
        LocalContainerEntityManagerFactoryBean factoryBean =
                new LocalContainerEntityManagerFactoryBean();
        factoryBean.setDataSource(getemployeeManagementDataSource());
        factoryBean.setPackagesToScan("com.deloitte.employee.infra.entities");
        factoryBean.setJpaVendorAdapter(vendorAdapter);
        return factoryBean;
    }

    /**
     * Transaction Manager for Employee management db.
     *
     * @param employeeManagementEntityManagerFactory : ControlTower Entity Manager factory
     * @return : control tower transaction manager
     */
    @Bean(name = "transactionManager")
    public PlatformTransactionManager getemployeeManagementTransactionManager(
            @Qualifier("employeeManagementEntityManagerFactoryBean")
            EntityManagerFactory employeeManagementEntityManagerFactory) {
        return new JpaTransactionManager(employeeManagementEntityManagerFactory);
    }

    /**
     * JdbcTemplate for Employee management db.
     *
     * @return JdbcTemplate: JdbcTemplate for getting db connection for Employee management db
     */
    @Bean(name = "employeeManagementJdbcTemplate")
    public JdbcTemplate getJdbcTemplate() {
        return new JdbcTemplate(getemployeeManagementDataSource());
    }

    @Bean(name = "employeeManagementNamedParameterJdbcTemplate")
    public NamedParameterJdbcTemplate getNamedParameterJdbcTemplate() {
        return new NamedParameterJdbcTemplate(getemployeeManagementDataSource());
    }
}