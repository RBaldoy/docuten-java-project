package com.docuten.project.service;

import com.docuten.project.model.dto.DocumentDetailsResponse;
import com.docuten.project.exception.DocumentDetailsException;

/**
 * Service interface for retrieving document details.
 */
public interface IDocumentDetailsService {

    /**
     * Retrieves the details of a document by its ID.
     * @param documentId the ID of the document to retrieve details for
     * @return the details of the document as a DocumentDetailsResponseDTO
     * @throws DocumentDetailsException if there is an error retrieving the document details
     */
    DocumentDetailsResponse getDocumentDetails(Long documentId) throws DocumentDetailsException;

}
