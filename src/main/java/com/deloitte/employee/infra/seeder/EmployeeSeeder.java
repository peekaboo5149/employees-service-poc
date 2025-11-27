package com.deloitte.employee.infra.seeder;

import com.deloitte.employee.application.config.EmployeeManagementProperties;
import com.deloitte.employee.presentation.dto.request.EmployeeDetailInput;
import com.deloitte.employee.presentation.service.IEmployeeManagementService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
class EmployeeSeeder {
    private final IEmployeeManagementService employeeManagementService;
    private final ObjectMapper mapper;
    private final EmployeeManagementProperties properties;

    @EventListener(ApplicationReadyEvent.class)
    public void seedEmployees() {
        try {
            if (!properties.isSeed()) {
                log.info("Seeding is disabled");
                return;
            }

            log.info("Seeding employees from JSON...");

            InputStream inputStream = getClass()
                    .getClassLoader()
                    .getResourceAsStream("content/employees.json");

            if (inputStream == null) {
                log.error("employees.json NOT FOUND in resources/content/");
                return;
            }

            List<EmployeeDetailInput> employees = mapper.readValue(
                    inputStream,
                    new TypeReference<>() {
                    }
            );

            for (EmployeeDetailInput employee : employees) {
                employeeManagementService.createEmployee(employee);
                log.info("Seeded employee: {}", employee.getEmail());
            }

            log.info("Employee seeding COMPLETED, total: {}", employees.size());

        } catch (Exception e) {
            log.error("Error during employee seeding", e);
        }
    }
}
