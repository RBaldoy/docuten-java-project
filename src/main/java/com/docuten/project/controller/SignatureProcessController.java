package com.docuten.project.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.docuten.project.model.dto.SignatureProcessRequest;
import com.docuten.project.service.ISignatureProcessService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * Controller for handling signature process related requests
 * SignatureProcessController
 */
@RestController
@RequestMapping ("/signature-process")
public class SignatureProcessController {

    private final ISignatureProcessService signatureProcessService;

    public SignatureProcessController(ISignatureProcessService signatureProcessService) {
        this.signatureProcessService = signatureProcessService;
    }

    @PostMapping("/create")
    @Operation (summary = "Crea un proceso de firma", description = "Crea un nuevo proceso de firma basado en la solicitud proporcionada", responses = {
            @ApiResponse(responseCode = "200", description = "Proceso de firma creado con éxito"),
            @ApiResponse(responseCode = "400", description = "Datos de solicitud inválidos")
    })
    public ResponseEntity<Long> createSignatureProcess(@Valid @RequestBody SignatureProcessRequest request) {
        Long processId = signatureProcessService.createProcess(request);
        return ResponseEntity.ok(processId);
    }

    @GetMapping("/verify-signature/{processId}")
    public ResponseEntity<Boolean> verifySignature(@NotNull @PathVariable Long processId) {
        boolean isSignatureValid = signatureProcessService.verifySignature(processId);
        return ResponseEntity.ok(isSignatureValid);
    }
}
