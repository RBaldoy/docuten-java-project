package com.docuten.project.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import jakarta.validation.ConstraintViolationException;

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

    @ExceptionHandler({ HandlerMethodValidationException.class, ConstraintViolationException.class })
    public ResponseEntity<ErrorResponseDTO> handleParameterValidation(Exception e) {
        return ResponseEntity.badRequest().body(new ErrorResponseDTO(
                "El identificador del documento es obligatorio."));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDTO> handleArgumentTypeMismatch(MethodArgumentTypeMismatchException e) {
        return ResponseEntity.badRequest().body(new ErrorResponseDTO(
                "El parámetro '" + e.getName() + "' tiene un formato inválido."));
    }

    @ExceptionHandler(DocumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleDocumentDetailsException(DocumentException e) {
        return ResponseEntity.badRequest().body(new ErrorResponseDTO(e.getMessage()));
    }

    @ExceptionHandler(SignDocumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleSignDocumentException(SignDocumentException e) {
        return ResponseEntity.badRequest().body(new ErrorResponseDTO(e.getMessage()));
    }
}
