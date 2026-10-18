package com.docuten.project.service;

import com.docuten.project.model.dto.SignDocumentRequest;
import com.docuten.project.model.dto.SignDocumentResponse;
import com.docuten.project.exception.SignDocumentException;
/**
 * Service interface for handling document signing operations.
 * ISignDocumentService
 */
public interface ISignDocumentService {

    /**
     * Signs a PDF document with the given ID and request details.
     *
     * @param id The ID of the PDF document to be signed.
     * @param request The request containing signing details.
     * @return The response containing the result of the signing operation.
     * @throws SignDocumentException if there is an error during the signing process.
     */
    SignDocumentResponse signPdfDocument(Long id, SignDocumentRequest request) throws SignDocumentException;

}
