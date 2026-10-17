package com.docuten.project.controller.dto;

import java.util.Date;
import java.util.List;

import com.docuten.project.model.ProcessTypeEnum;

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

    private String base64PDF;
    private List<SignerDTO> signers;
    private ProcessTypeEnum processType;
    private Date expirationDate;

}
