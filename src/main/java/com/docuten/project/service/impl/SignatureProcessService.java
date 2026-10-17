package com.docuten.project.service.impl;

import com.docuten.project.controller.dto.SignatureProcessRequest;
import com.docuten.project.service.ISignatureProcessService;

public class SignatureProcessService implements ISignatureProcessService {

    @Override
    public Long createProcess(SignatureProcessRequest request) {
        // Implement the logic to create a new signature process based on the request
        // For now, return a dummy ID (e.g., 1L)
        return 1L;
    }

}
