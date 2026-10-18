package com.docuten.project.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.docuten.project.exception.SignatureProcessException;
import com.docuten.project.model.dto.SignatureProcessRequest;
import com.docuten.project.service.ISignatureProcessService;

@ExtendWith(MockitoExtension.class)
class SignatureProcessControllerTest {

    @Mock
    private ISignatureProcessService signatureProcessService;

    @InjectMocks
    private SignatureProcessController controller;

    @Test
    void createSignatureProcessReturnsServiceId() {
        SignatureProcessRequest request = SignatureProcessRequest.builder().build();
        when(signatureProcessService.createProcess(request)).thenReturn(42L);

        ResponseEntity<Long> response = controller.createSignatureProcess(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(42L, response.getBody());
        verify(signatureProcessService).createProcess(request);
    }

    @Test
    void createSignatureProcessPropagatesServiceException() {
        SignatureProcessRequest request = SignatureProcessRequest.builder().build();
        SignatureProcessException failure = new SignatureProcessException("invalid request");
        when(signatureProcessService.createProcess(request)).thenThrow(failure);

        SignatureProcessException thrown = Assertions.assertThrows(
                SignatureProcessException.class, () -> controller.createSignatureProcess(request));

        assertSame(failure, thrown);
        verify(signatureProcessService).createProcess(request);
    }

    @Test
    void verifySignatureReturnsServiceResult() {
        when(signatureProcessService.verifySignature(5L)).thenReturn(true);

        ResponseEntity<Boolean> response = controller.verifySignature(5L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(true, response.getBody());
    }

    @Test
    void verifySignaturePropagatesServiceException() {
        SignatureProcessException failure = new SignatureProcessException("not found");
        when(signatureProcessService.verifySignature(5L)).thenThrow(failure);

        assertSame(failure, Assertions.assertThrows(SignatureProcessException.class,
                () -> controller.verifySignature(5L)));
    }
}
