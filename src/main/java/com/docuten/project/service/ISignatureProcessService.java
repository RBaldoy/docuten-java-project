package com.docuten.project.service;

import com.docuten.project.exception.SignatureProcessException;
import com.docuten.project.model.dto.SignatureProcessRequest;

/**
 * Service interface for handling signature process operations
 * ISignatureProcessService
 */
public interface ISignatureProcessService {

    /**
     * Creates a new signature process based on the provided request
     * @param request The signature process request containing necessary information
     * @return The ID of the created signature process
     * @throws SignatureProcessException if there is an error during the creation of the signature process
     */
    Long createProcess(SignatureProcessRequest request)  throws SignatureProcessException;

    /**
     * Verifies the signature of the specified signature process
     * @param processId The ID of the signature process to verify
     * @return true if the signature is valid, false otherwise
     * @throws SignatureProcessException if there is an error during the verification of the signature
     */
    boolean verifySignature(Long processId) throws SignatureProcessException;

}
