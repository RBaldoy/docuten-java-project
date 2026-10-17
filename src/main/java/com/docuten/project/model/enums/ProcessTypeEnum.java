package com.docuten.project.model.enums;

import java.util.List;

/**
 * Enum representing the type of signature process
 */
public enum ProcessTypeEnum {

    SEQUENTIAL,
    PARALLEL;

    public List<String> getProcessTypes() {
        return List.of(SEQUENTIAL.name(), PARALLEL.name());
    }
}
