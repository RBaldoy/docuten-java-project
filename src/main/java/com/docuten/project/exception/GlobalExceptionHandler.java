package com.docuten.project.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.docuten.project.model.dto.ErrorResponseDTO;

@RestControllerAdvice 
public class GlobalExceptionHandler {

    @ExceptionHandler(SignatureProcessException.class)
    public ResponseEntity<ErrorResponseDTO> handleSignatureProcessException(SignatureProcessException e) {
        return ResponseEntity.badRequest().body(new ErrorResponseDTO(e.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidRequestBody(HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest().body(new ErrorResponseDTO(
                "El cuerpo de la petición contiene un formato inválido."));
    }
}
