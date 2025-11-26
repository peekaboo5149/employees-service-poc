package com.deloitte.employee.infra.repositories;

import com.deloitte.employee.infra.entities.EmployeeJPAEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeJPARepository extends JpaRepository<EmployeeJPAEntity, String>, JpaSpecificationExecutor<EmployeeJPAEntity> {
    boolean existsByEmail(String email);
}
