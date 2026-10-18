package com.docuten.project.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.docuten.project.model.enums.SignatoryStatusEnum;

/**
 * Represents a signatory in a document details response.
 * DocumentDetailsSignatoryDTO
 */
@Getter
@Setter 
@AllArgsConstructor 
@NoArgsConstructor 
@Builder
public class DocumentDetailsSignatoryDTO {

    private String name;
    private String email;
    private int signOrder;
    private SignatoryStatusEnum signStatus;
}
