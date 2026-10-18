package com.docuten.project.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.docuten.project.model.dto.SignDocumentRequest;
import com.docuten.project.model.dto.SignDocumentResponse;
import com.docuten.project.service.ISignDocumentService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;

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

    @PostMapping("/pdf/{id}")
    public ResponseEntity<SignDocumentResponse> signPdfDocument(@PathVariable("id") Long id, @RequestBody SignDocumentRequest request) {
        // Implement the logic to sign the PDF document with the given ID
        SignDocumentResponse response = signDocumentService.signPdfDocument(id, request);
        return ResponseEntity.ok(response);
    }
}
