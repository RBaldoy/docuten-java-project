package com.docuten.project.model.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.docuten.project.model.enums.ProcessTypeEnum;
import com.docuten.project.model.enums.SignatureProcessStatusEnum;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) for the SignatureProcess entity
 * SignatureProcessDTO
 */
@Getter 
@Setter 
@AllArgsConstructor 
@NoArgsConstructor 
@Builder 
public class SignatureProcessDTO {

    private Long id;
    private ProcessTypeEnum processType;
    private SignatureProcessStatusEnum status;
    private LocalDateTime creationDate;
    private LocalDateTime expirationDate;
    private LocalDateTime statusUpdateDate;
    private DocumentDTO document;
    private List<SignatoryDTO> signatories;
}
