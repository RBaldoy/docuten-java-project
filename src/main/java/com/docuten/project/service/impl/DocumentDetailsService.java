package com.docuten.project.service.impl;

import org.springframework.stereotype.Service;
import java.util.List;

import com.docuten.project.service.IDocumentDetailsService;
import com.docuten.project.model.dto.DocumentDetailsResponse;
import com.docuten.project.model.dto.DocumentDetailsSignatoryDTO;
import com.docuten.project.exception.DocumentDetailsException;
import com.docuten.project.model.Document;
import com.docuten.project.model.Signatory;
import com.docuten.project.model.SignatureProcess;
import com.docuten.project.repository.DocumentRepository;

@Service
public class DocumentDetailsService implements IDocumentDetailsService {

    private final DocumentRepository documentRepository;

    public DocumentDetailsService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    @Override
    public DocumentDetailsResponse getDocumentDetails(Long documentId) throws DocumentDetailsException {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new DocumentDetailsException("Document not found"));
        SignatureProcess signatureProcess = document.getSignatureProcess();
        List<Signatory> signatories = signatureProcess.getSignatories();

        List<DocumentDetailsSignatoryDTO> signatoryDTO = signatories.stream().map(signatory -> DocumentDetailsSignatoryDTO.builder()
                .name(signatory.getName())
                .email(signatory.getEmail())
                .signOrder(signatory.getSignOrder())
                .signStatus(signatory.getSignStatus())
                .build())
                .toList();

        return DocumentDetailsResponse.builder()
                .id(document.getId())
                .fileName(document.getFileName())
                .hash(document.getHash())
                .size(document.getSize())
                .creationDate(document.getCreationDate())
                .processStatus(signatureProcess.getStatus())
                .signatories(signatoryDTO)
                .build();
    }

}
