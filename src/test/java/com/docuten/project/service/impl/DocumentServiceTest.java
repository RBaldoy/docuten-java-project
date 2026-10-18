package com.docuten.project.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.docuten.project.exception.DocumentException;
import com.docuten.project.model.Document;
import com.docuten.project.model.Signatory;
import com.docuten.project.model.SignatureProcess;
import com.docuten.project.model.dto.DocumentDetailsResponse;
import com.docuten.project.model.enums.SignatoryStatusEnum;
import com.docuten.project.model.enums.SignatureProcessStatusEnum;
import com.docuten.project.repository.DocumentRepository;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @InjectMocks
    private DocumentService service;

    @Test
    void getDocumentDetailsMapsDocumentProcessAndSignatories() {
        LocalDateTime creation = LocalDateTime.now();
        Document document = document(SignatureProcessStatusEnum.IN_PROGRESS, signatory(7L, "Ada", 1));
        document.setCreationDate(creation);
        when(documentRepository.findById(1L)).thenReturn(Optional.of(document));

        DocumentDetailsResponse response = service.getDocumentDetails(1L);

        assertEquals(1L, response.getId());
        assertEquals("a.pdf", response.getFileName());
        assertEquals("hash", response.getHash());
        assertEquals(10, response.getSize());
        assertEquals(creation, response.getCreationDate());
        assertEquals(SignatureProcessStatusEnum.IN_PROGRESS, response.getProcessStatus());
        assertEquals(1, response.getSignatories().size());
        assertEquals("Ada", response.getSignatories().get(0).getName());
        assertEquals("ada@example.com", response.getSignatories().get(0).getEmail());
        assertEquals(1, response.getSignatories().get(0).getSignOrder());
        assertEquals(SignatoryStatusEnum.PENDING, response.getSignatories().get(0).getSignStatus());
    }

    @Test
    void getDocumentDetailsFailsWhenDocumentDoesNotExist() {
        when(documentRepository.findById(1L)).thenReturn(Optional.empty());

        DocumentException exception = assertThrows(DocumentException.class, () -> service.getDocumentDetails(1L));

        assertEquals("Document not found", exception.getMessage());
    }

    @Test
    void rejectDocumentMarksSignatoryAndProcessAsRejectedAndSaves() {
        Signatory signatory = signatory(7L, "Ada", 1);
        Document document = document(SignatureProcessStatusEnum.PENDING, signatory);
        when(documentRepository.findById(1L)).thenReturn(Optional.of(document));

        service.rejectDocument(1L, 7L, "not mine");

        SignatureProcess process = document.getSignatureProcess();
        assertEquals(SignatoryStatusEnum.REJECTED, signatory.getSignStatus());
        assertEquals(SignatureProcessStatusEnum.REJECTED, process.getStatus());
        assertEquals("not mine", process.getRejectionReason());
        assertNotNull(process.getStatusUpdateDate());
        verify(documentRepository).save(document);
    }

    @Test
    void rejectDocumentFailsWhenDocumentDoesNotExist() {
        when(documentRepository.findById(1L)).thenReturn(Optional.empty());

        DocumentException exception = assertThrows(DocumentException.class,
                () -> service.rejectDocument(1L, 7L, "reason"));

        assertEquals("Document not found", exception.getMessage());
        verify(documentRepository, never()).save(any(Document.class));
    }

    @Test
    void rejectDocumentFailsWhenProcessAlreadyRejected() {
        Document document = document(SignatureProcessStatusEnum.REJECTED, signatory(7L, "Ada", 1));
        when(documentRepository.findById(1L)).thenReturn(Optional.of(document));

        DocumentException exception = assertThrows(DocumentException.class,
                () -> service.rejectDocument(1L, 7L, "reason"));

        assertEquals("Document has already been rejected", exception.getMessage());
        verify(documentRepository, never()).save(any(Document.class));
    }

    @Test
    void rejectDocumentFailsWhenSignatoryIsNotInProcess() {
        Document document = document(SignatureProcessStatusEnum.PENDING, signatory(7L, "Ada", 1));
        when(documentRepository.findById(1L)).thenReturn(Optional.of(document));

        DocumentException exception = assertThrows(DocumentException.class,
                () -> service.rejectDocument(1L, 99L, "reason"));

        assertEquals("Signatory not found", exception.getMessage());
        assertEquals(SignatureProcessStatusEnum.PENDING, document.getSignatureProcess().getStatus());
        verify(documentRepository, never()).save(any(Document.class));
    }

    private static Document document(SignatureProcessStatusEnum status, Signatory... signatories) {
        SignatureProcess process = new SignatureProcess();
        process.setId(5L);
        process.setStatus(status);
        process.setSignatories(List.of(signatories));
        Document document = new Document();
        document.setId(1L);
        document.setFileName("a.pdf");
        document.setHash("hash");
        document.setSize(10);
        document.setSignatureProcess(process);
        return document;
    }

    private static Signatory signatory(Long id, String name, int order) {
        Signatory signatory = new Signatory();
        signatory.setId(id);
        signatory.setName(name);
        signatory.setEmail("ada@example.com");
        signatory.setSignOrder(order);
        signatory.setSignStatus(SignatoryStatusEnum.PENDING);
        return signatory;
    }
}
