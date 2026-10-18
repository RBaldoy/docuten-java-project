package com.docuten.project.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.docuten.project.exception.DocumentException;
import com.docuten.project.model.dto.DocumentDetailsResponse;
import com.docuten.project.service.IDocumentService;

@ExtendWith(MockitoExtension.class)
class DocumentControllerTest {

    @Mock
    private IDocumentService documentService;

    @InjectMocks
    private DocumentController controller;

    @Test
    void getDocumentDetailsReturnsServiceResponse() {
        DocumentDetailsResponse details = DocumentDetailsResponse.builder().id(1L).fileName("a.pdf").build();
        when(documentService.getDocumentDetails(1L)).thenReturn(details);

        ResponseEntity<DocumentDetailsResponse> response = controller.getDocumentDetails(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(details, response.getBody());
    }

    @Test
    void getDocumentDetailsPropagatesServiceException() {
        DocumentException failure = new DocumentException("Document not found");
        when(documentService.getDocumentDetails(1L)).thenThrow(failure);

        assertSame(failure, assertThrows(DocumentException.class, () -> controller.getDocumentDetails(1L)));
    }

    @Test
    void rejectDocumentDelegatesToServiceAndReturnsEmptyOk() {
        ResponseEntity<Void> response = controller.rejectDocument(1L, 2L, "reason");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
        verify(documentService).rejectDocument(1L, 2L, "reason");
    }

    @Test
    void rejectDocumentPropagatesServiceException() {
        DocumentException failure = new DocumentException("Signatory not found");
        doThrow(failure).when(documentService).rejectDocument(1L, 2L, "reason");

        assertSame(failure, assertThrows(DocumentException.class, () -> controller.rejectDocument(1L, 2L, "reason")));
    }
}
