package com.docuten.project.model.dto;

import java.util.Date;
import java.util.List;

import com.docuten.project.model.enums.ProcessTypeEnum;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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

    @NotBlank (message = "El PDF codificado es obligatorio")
    private String encodedPDF;

    @NotEmpty (message = "La lista de firmantes es obligatoria")
    @Valid
    private List<SignerDTO> signers;

    @NotNull (message = "El tipo de proceso es obligatorio")
    private ProcessTypeEnum processType;

    @NotNull (message = "La fecha de expiración es obligatoria")
    @Future (message = "La fecha de expiración debe ser futura")
    private Date expirationDate;

}
