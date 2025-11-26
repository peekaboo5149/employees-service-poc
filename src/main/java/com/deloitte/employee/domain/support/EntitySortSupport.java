package com.deloitte.employee.domain.support;

public interface EntitySortSupport<T extends Enum<?>> {

    String mapSortFieldToColumn(T field);

}
