package com.docuten.project.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.docuten.project.exception.SignDocumentException;
import com.docuten.project.model.dto.KeyPairResponseDTO;
import com.docuten.project.model.dto.SignDocumentRequest;
import com.docuten.project.model.dto.SignDocumentResponse;
import com.docuten.project.service.ISignDocumentService;

@ExtendWith(MockitoExtension.class)
class SignDocumentControllerTest {

    @Mock
    private ISignDocumentService signDocumentService;

    @InjectMocks
    private SignDocumentController controller;

    @Test
    void generateKeyPairReturnsServiceResponse() {
        KeyPairResponseDTO keys = KeyPairResponseDTO.builder().publicKeyBase64("pub").build();
        when(signDocumentService.generateKeyPair(3L)).thenReturn(keys);

        ResponseEntity<KeyPairResponseDTO> response = controller.generateKeyPair(3L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(keys, response.getBody());
    }

    @Test
    void generateKeyPairPropagatesServiceException() {
        SignDocumentException failure = new SignDocumentException("Signatory not found");
        when(signDocumentService.generateKeyPair(3L)).thenThrow(failure);

        assertSame(failure, assertThrows(SignDocumentException.class, () -> controller.generateKeyPair(3L)));
    }

    @Test
    void signPdfDocumentReturnsServiceResponse() {
        SignDocumentRequest request = SignDocumentRequest.builder().privateKeyBase64("key").build();
        SignDocumentResponse expected = SignDocumentResponse.builder().signedDocumentPath("/tmp/a-signed-V1").build();
        when(signDocumentService.signPdfDocument(1L, 2L, request)).thenReturn(expected);

        ResponseEntity<SignDocumentResponse> response = controller.signPdfDocument(1L, 2L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expected, response.getBody());
    }

    @Test
    void signPdfDocumentPropagatesServiceException() {
        SignDocumentRequest request = SignDocumentRequest.builder().privateKeyBase64("key").build();
        SignDocumentException failure = new SignDocumentException("Document not found");
        when(signDocumentService.signPdfDocument(1L, 2L, request)).thenThrow(failure);

        assertSame(failure,
                assertThrows(SignDocumentException.class, () -> controller.signPdfDocument(1L, 2L, request)));
    }
}
