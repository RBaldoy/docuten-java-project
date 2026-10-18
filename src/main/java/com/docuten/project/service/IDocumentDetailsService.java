package com.docuten.project.service;

import com.docuten.project.model.dto.DocumentDetailsResponseDTO;

/**
 * Service interface for retrieving document details.
 */
public interface IDocumentDetailsService {

    /**
     * Retrieves the details of a document by its ID.
     * @param documentId the ID of the document to retrieve details for
     * @return the details of the document as a DocumentDTO
     */
    DocumentDetailsResponseDTO getDocumentDetails(Long documentId);

}
