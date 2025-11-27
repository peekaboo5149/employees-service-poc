package com.deloitte.employee.presentation.service.impl;

import com.deloitte.employee.presentation.dto.request.QueryRequest;
import com.deloitte.employee.presentation.dto.response.EmployeeDetail;
import com.deloitte.employee.presentation.dto.response.GenericListResponse;
import com.deloitte.employee.presentation.service.ResponseMapper;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.deloitte.employee.presentation.helper.Constants.EMPLOYEE_LIST_QUERY_IMPL;

@Component(EMPLOYEE_LIST_QUERY_IMPL)
class EmployeeResponseMapper implements ResponseMapper<EmployeeDetail> {

    @Override
    public GenericListResponse<EmployeeDetail> toResponse(QueryRequest originalQuery,
                                                          QueryRequest scaledQuery,
                                                          List<EmployeeDetail> employees) {
        int originalSize = originalQuery.getSize();
        boolean hasNext = employees.size() > originalSize;

        List<EmployeeDetail> trimmed = hasNext
                ? employees.subList(0, originalSize)
                : employees;

        var meta = GenericListResponse.Meta.builder()
                .page(originalQuery.getPage())
                .size(originalSize)
                .hasNext(hasNext)
                .build();

        return GenericListResponse.<EmployeeDetail>builder()
                .data(trimmed)
                .meta(meta)
                .build();
    }
}
