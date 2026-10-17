package com.docuten.project.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.docuten.project.model.dto.ErrorResponseDTO;

@RestControllerAdvice 
public class GlobalExceptionHandler {

    @ExceptionHandler(SignatureProcessException.class)
    public ResponseEntity<ErrorResponseDTO> handleSignatureProcessException(SignatureProcessException e) {
        return ResponseEntity.badRequest().body(new ErrorResponseDTO(e.getMessage()));
    }

}
