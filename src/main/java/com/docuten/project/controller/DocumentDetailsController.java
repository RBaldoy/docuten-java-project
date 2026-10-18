package com.docuten.project.controller;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import com.docuten.project.model.dto.DocumentDetailsResponse;
import com.docuten.project.service.IDocumentDetailsService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

/**
 * Controller for handling document details requests.
 * DocumentDetailsController
 */
@RestController
@RequestMapping("/documents")
public class DocumentDetailsController {

    private final IDocumentDetailsService documentDetailsService;

    public DocumentDetailsController(IDocumentDetailsService documentDetailsService) {
        this.documentDetailsService = documentDetailsService;
    }

    @GetMapping("show/{id}")
    @Operation (summary = "Consultar documento", description = "Consulta los detalles de un documento a partir de su ID", responses = {
            @ApiResponse(responseCode = "200", description = "Detalles del documento recuperados con éxito"),
            @ApiResponse(responseCode = "400", description = "Datos de solicitud inválidos")
    })
    public ResponseEntity<DocumentDetailsResponse> getDocumentDetails(@NotNull @PathVariable("id") Long id) {
        return ResponseEntity.ok(documentDetailsService.getDocumentDetails(id));
    }


}
