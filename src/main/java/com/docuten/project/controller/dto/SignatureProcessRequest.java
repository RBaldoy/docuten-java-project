package com.docuten.project.controller.dto;

import java.util.Date;
import java.util.List;

import com.docuten.project.model.enums.ProcessTypeEnum;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO class for signature process request
 * SignatureProcessRequest
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SignatureProcessRequest {

    @NotNull
    private String base64PDF;

    @NotNull
    private List<SignerDTO> signers;

    @NotNull
    private ProcessTypeEnum processType;
    
    @NotNull
    private Date expirationDate;

}
