package com.docuten.project.service;

import com.docuten.project.model.dto.KeyPairResponseDTO;
import com.docuten.project.model.dto.SignDocumentRequest;
import com.docuten.project.model.dto.SignDocumentResponse;
import com.docuten.project.exception.SignDocumentException;
import com.docuten.project.exception.KeyManagerException;
/**
 * Service interface for handling document signing operations.
 * ISignDocumentService
 */
public interface ISignDocumentService {

    /**
     * Signs a PDF document with the given ID and request details.
     *
     * @param documentId The ID of the signing process for the PDF document.
     * @param signatoryId The ID of the signatory.
     * @param request signing details.
     * @return result of the signing operation.
     * @throws SignDocumentException if there is an error during the signing process.
     */
    SignDocumentResponse signPdfDocument(Long documentId, Long signatoryId, SignDocumentRequest request) throws SignDocumentException;

    /**
     * Generates a key pair for the given signatory ID.
     *
     * @param signatoryId The ID of the signatory
     * @return key pair in Base64 format.
     * @throws SignDocumentException if there is an error during key pair generation.
     * @throws KeyManagerException if there is an error with the key management service.
     */
    KeyPairResponseDTO generateKeyPair(Long signatoryId) throws SignDocumentException, KeyManagerException;

}
