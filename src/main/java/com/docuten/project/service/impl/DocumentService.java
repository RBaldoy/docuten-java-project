package com.docuten.project.service.impl;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import com.docuten.project.service.IDocumentService;

import jakarta.transaction.Transactional;

import com.docuten.project.model.dto.DocumentDetailsResponse;
import com.docuten.project.model.dto.DocumentDetailsSignatoryDTO;
import com.docuten.project.model.enums.SignatoryStatusEnum;
import com.docuten.project.model.enums.SignatureProcessStatusEnum;
import com.docuten.project.exception.DocumentException;
import com.docuten.project.model.Document;
import com.docuten.project.model.Signatory;
import com.docuten.project.model.SignatureProcess;
import com.docuten.project.repository.DocumentRepository;

@Service
public class DocumentService implements IDocumentService {

    private final DocumentRepository documentRepository;

    public DocumentService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    @Override
    public DocumentDetailsResponse getDocumentDetails(Long documentId) throws DocumentException {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new DocumentException("Document not found"));
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

    @Override
    @Transactional
    public void rejectDocument(Long documentId, Long signatoryId, String reason) throws DocumentException {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new DocumentException("Document not found"));
        
        SignatureProcess signatureProcess = document.getSignatureProcess();

        if (SignatureProcessStatusEnum.REJECTED.equals(signatureProcess.getStatus())) {
            throw new DocumentException("Document has already been rejected");
        }

        Signatory signatory = signatureProcess.getSignatories().stream()
                .filter(s -> s.getId().equals(signatoryId))
                .findFirst()
                .orElseThrow(() -> new DocumentException("Signatory not found"));

        signatory.setSignStatus(SignatoryStatusEnum.REJECTED);
        signatureProcess.setRejectionReason(reason);
        signatureProcess.setStatus(SignatureProcessStatusEnum.REJECTED);
        signatureProcess.setStatusUpdateDate(LocalDateTime.now(ZoneId.systemDefault()));
        documentRepository.save(document);
    }

}
