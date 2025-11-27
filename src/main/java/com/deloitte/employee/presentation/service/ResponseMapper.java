package com.deloitte.employee.presentation.service;

import com.deloitte.employee.presentation.dto.request.QueryRequest;
import com.deloitte.employee.presentation.dto.response.GenericListResponse;

import java.util.List;

public interface ResponseMapper<T> {
    GenericListResponse<T> toResponse(QueryRequest originalQuery,QueryRequest scaled, List<T> employee);

    default QueryRequest scaled(QueryRequest req) {
        return req.toBuilder().size(req.getSize() + 1).build();
    }
}
