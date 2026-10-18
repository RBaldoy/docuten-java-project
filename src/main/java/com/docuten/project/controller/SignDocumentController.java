package com.docuten.project.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.docuten.project.model.dto.KeyPairResponseDTO;
import com.docuten.project.model.dto.SignDocumentRequest;
import com.docuten.project.model.dto.SignDocumentResponse;
import com.docuten.project.service.ISignDocumentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.constraints.NotNull;

import org.springframework.web.bind.annotation.PostMapping;

/**
 * Controller for handling document signing requests.
 * SignDocumentController
 */
@RestController
@RequestMapping("/sign-document")
public class SignDocumentController {
    
    private final ISignDocumentService signDocumentService;

    public SignDocumentController(ISignDocumentService signDocumentService) {
        this.signDocumentService = signDocumentService;
    }

    @PostMapping("/keys/{signatoryId}")
    @Operation (summary = "Genera las claves del firmante", description = "Genera un nuevo par de claves para el firmante especificado", responses = {
            @ApiResponse(responseCode = "200", description = "Claves generadas con éxito"),
            @ApiResponse(responseCode = "400", description = "Datos de solicitud inválidos")
    })
    public ResponseEntity<KeyPairResponseDTO> generateKeyPair(@NotNull @PathVariable("signatoryId") Long signatoryId) {
        
        KeyPairResponseDTO response = signDocumentService.generateKeyPair(signatoryId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/pdf/{id}")
    @Operation (summary = "Firma un documento PDF", description = "Firma un documento PDF por su identificador de proceso de firma", responses = {
            @ApiResponse(responseCode = "200", description = "Documento firmado con éxito"),
            @ApiResponse(responseCode = "400", description = "Datos de solicitud inválidos")
    })
    public ResponseEntity<SignDocumentResponse> signPdfDocument(@NotNull @PathVariable("id") Long id, @RequestBody SignDocumentRequest request) {

        SignDocumentResponse response = signDocumentService.signPdfDocument(id, request);
        return ResponseEntity.ok(response);
    }
}
