package com.docuten.project.controller;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import com.docuten.project.model.dto.DocumentDetailsResponse;
import com.docuten.project.service.IDocumentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

/**
 * Controller for handling document details requests.
 * DocumentController
 */
@RestController
@RequestMapping("/documents")
public class DocumentController {

    private final IDocumentService documentService;

    public DocumentController(IDocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping("/show/{id}")
    @Operation (summary = "Consultar documento", description = "Consulta los detalles de un documento a partir de su ID", responses = {
            @ApiResponse(responseCode = "200", description = "Detalles del documento recuperados con éxito"),
            @ApiResponse(responseCode = "400", description = "Datos de solicitud inválidos")
    })
    public ResponseEntity<DocumentDetailsResponse> getDocumentDetails(@NotNull @PathVariable("id") Long id) {
        return ResponseEntity.ok(documentService.getDocumentDetails(id));
    }

    @PostMapping("reject/{documentId}/user/{signatoryId}")
    @Operation(summary = "Rechazar documento", description = "Rechaza un documento a partir de su ID", responses = {
            @ApiResponse(responseCode = "200", description = "Documento rechazado con éxito"),
            @ApiResponse(responseCode = "400", description = "Datos de solicitud inválidos")
    })
    public ResponseEntity<Void> rejectDocument(@NotNull @PathVariable("documentId") Long documentId, @NotNull @PathVariable("signatoryId") Long signatoryId, @RequestBody String reason) {
        documentService.rejectDocument(documentId, signatoryId, reason);
        return ResponseEntity.ok().build();
    }




}
