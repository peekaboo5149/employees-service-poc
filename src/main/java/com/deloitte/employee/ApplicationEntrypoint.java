package com.deloitte.employee;

import com.deloitte.employee.infra.config.DataSourceConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;

//@SpringBootApplication(exclude = DataSourceConfig.class)
//@ComponentScan(basePackages = {"com.deloitte.employee"})
//@EntityScan(basePackages = {"com.deloitte.employee.infra.entities"})
@SpringBootApplication
public class ApplicationEntrypoint {

	public static void main(String[] args) {
		SpringApplication.run(ApplicationEntrypoint.class, args);
    }

}
