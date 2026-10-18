package com.docuten.project.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response DTO for key pair generation containing the private key, public key, and certificate in Base64 format.
 * KeyPairResponseDTO
 */
@Getter
@Setter 
@AllArgsConstructor 
@NoArgsConstructor 
@Builder 
public class KeyPairResponseDTO {

    private String privateKeyBase64;
    private String publicKeyBase64;
    private String certificateBase64;
}
