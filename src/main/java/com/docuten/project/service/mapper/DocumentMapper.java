package com.docuten.project.service.mapper;

import org.springframework.stereotype.Component;

import com.docuten.project.model.Document;
import com.docuten.project.model.dto.DocumentDTO;

import lombok.RequiredArgsConstructor;

/**
 * Mapper for converting between Document and DocumentDTO
 * DocumentMapper
 */
@Component
@RequiredArgsConstructor 
public class DocumentMapper {

    private final SignatureProcessMapper signatureProcessMapper;

    /**
     * Map a Document entity to a DocumentDTO.
     * @param document document entity
     * @return DocumentDTO
     */
    public DocumentDTO toDTO(Document document) {
        if (document == null) {
            return null;
        }

        DocumentDTO dto = DocumentDTO.builder()
                .id(document.getId())
                .fileName(document.getFileName())
                .storagePath(document.getStoragePath())
                .hash(document.getHash())
                .size(document.getSize())
                .creationDate(document.getCreationDate())
                .build();
        
        if (document.getSignatureProcess() != null) {
            dto.setSignatureProcess(signatureProcessMapper.toDTO(document.getSignatureProcess()));
        }
        return dto;
    }

    /**
     * Map a DocumentDTO to a Document entity.
     * @param documentDTO documentDTO
     * @return Document entity
     */
    public Document toEntity(DocumentDTO documentDTO) {
        if (documentDTO == null) {
            return null;
        }

        Document document = new Document();
        document.setFileName(documentDTO.getFileName());
        document.setStoragePath(documentDTO.getStoragePath());
        document.setHash(documentDTO.getHash());
        document.setSize(documentDTO.getSize());
        document.setCreationDate(documentDTO.getCreationDate());

        if (documentDTO.getSignatureProcess() != null) {
            document.setSignatureProcess(signatureProcessMapper.toEntity(documentDTO.getSignatureProcess()));
        }
        return document;
    }
}
