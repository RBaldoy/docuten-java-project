package com.docuten.project.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.docuten.project.controller.dto.SignatureProcessRequest;
import com.docuten.project.service.ISignatureProcessService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

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
    @Operation (summary = "Create a new signature process", description = "Creates a new signature process based on the provided request", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully created the signature process"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<Long> createSignatureProcess(@RequestBody SignatureProcessRequest request) {
        Long processId = signatureProcessService.createProcess(request);
        return ResponseEntity.ok(processId);
    }
}
