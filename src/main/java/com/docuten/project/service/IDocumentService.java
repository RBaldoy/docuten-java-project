package com.docuten.project.service;

import com.docuten.project.model.dto.DocumentDetailsResponse;
import com.docuten.project.exception.DocumentException;

/**
 * Service interface for retrieving document details.
 */
public interface IDocumentService {

    /**
     * Retrieves the details of a document by its ID.
     * @param documentId the ID of the document to retrieve details for
     * @return the details of the document as a DocumentDetailsResponseDTO
     * @throws DocumentException if there is an error retrieving the document details
     */
    DocumentDetailsResponse getDocumentDetails(Long documentId) throws DocumentException;

    /**
     * Rejects a document by its ID
     * @param documentId the ID of the document to reject
     * @param signatoryId the ID of the signatory rejecting the document
     * @param reason the reason for rejecting the document
     * @return void
     * @throws DocumentException if there is an error rejecting the document
     */
    void rejectDocument(Long documentId, Long signatoryId, String reason) throws DocumentException;

}
