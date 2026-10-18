package com.docuten.project.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request DTO for signing a document (private key in Base64 format).
 * SignDocumentRequest
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SignDocumentRequest {

    @NotBlank (message = "La clave privada es obligatoria")
    String privateKeyBase64;

}
