package com.docuten.project.service;

import org.springframework.stereotype.Service;

import com.docuten.project.controller.dto.SignatureProcessRequest;

/**
 * Service interface for handling signature process operations
 * ISignatureProcessService
 */
@Service
public interface ISignatureProcessService {

    /**
     * Creates a new signature process based on the provided request
     * @param request The signature process request containing necessary information
     * @return The ID of the created signature process
     */
    Long createProcess(SignatureProcessRequest request);

}
