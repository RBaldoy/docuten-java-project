package com.docuten.project.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO class for signer information
 * SignerDTO
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SignerDTO {

    @NotBlank (message = "El nombre del firmante es obligatorio")
    private String name;
   
    @NotBlank (message = "El correo electrónico del firmante es obligatorio")
    @Email (message = "El correo electrónico del firmante es inválido")
    private String email;
}
