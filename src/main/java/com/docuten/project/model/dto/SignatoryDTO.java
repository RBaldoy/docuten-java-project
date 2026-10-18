package com.docuten.project.model.dto;

import java.time.LocalDateTime;

import com.docuten.project.model.enums.SignatoryStatusEnum;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) for the Signatory entity
 * SignatoryDTO
 */
@Getter
@Setter 
@AllArgsConstructor 
@NoArgsConstructor 
@Builder 
public class SignatoryDTO {
    
    private Long id;
    private String name;
    private String email;
    private int signOrder;
    private SignatoryStatusEnum signStatus;
    private LocalDateTime signDate;
    private LocalDateTime creationDate;
    private SignatureProcessDTO signatureProcess;
}
