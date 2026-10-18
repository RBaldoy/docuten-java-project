package com.docuten.project.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.docuten.project.exception.SignatureProcessException;
import com.docuten.project.model.SignatureProcess;
import com.docuten.project.model.dto.SignatureProcessDTO;
import com.docuten.project.model.dto.SignatureProcessRequest;
import com.docuten.project.model.dto.SignerDTO;
import com.docuten.project.model.enums.ProcessTypeEnum;
import com.docuten.project.model.enums.SignatoryStatus;
import com.docuten.project.model.enums.SignatureProcessStatus;
import com.docuten.project.repository.SignatureProcessRepository;
import com.docuten.project.service.constants.Constants;
import com.docuten.project.service.mapper.SignatureProcessMapper;

@ExtendWith(MockitoExtension.class)
class SignatureProcessServiceTest {

    private static final byte[] PDF_CONTENT = "%PDF-1.7 test".getBytes(java.nio.charset.StandardCharsets.UTF_8);

    @TempDir
    Path storagePath;

    @Mock
    private SignatureProcessMapper signatureProcessMapper;

    @Mock
    private SignatureProcessRepository signatureProcessRepository;

    private SignatureProcessService service;

    @BeforeEach
    void setUpService() {
        service = new SignatureProcessService(signatureProcessMapper, signatureProcessRepository, storagePath.toString());
    }

    @Test
    void createProcessPersistsProcessAndStoresDocumentWithExpectedMetadata() throws Exception {
        SignatureProcessRequest request = request(signers(signer("Ada", "ada@example.com"),
                signer("Linus", "linus@example.com")));
        SignatureProcess persistedEntity = new SignatureProcess();
        when(signatureProcessMapper.toEntity(any(SignatureProcessDTO.class))).thenReturn(persistedEntity);
        when(signatureProcessRepository.save(persistedEntity)).thenAnswer(invocation -> {
            SignatureProcess entity = invocation.getArgument(0);
            entity.setId(73L);
            return entity;
        });

        Long id = service.createProcess(request);

        assertEquals(73L, id);
        Path storedFile = storagePath.resolve(request.getFileName());
        assertTrue(Files.exists(storedFile));
        assertTrue(java.util.Arrays.equals(PDF_CONTENT, Files.readAllBytes(storedFile)));

        ArgumentCaptor<SignatureProcessDTO> dtoCaptor = ArgumentCaptor.forClass(SignatureProcessDTO.class);
        verify(signatureProcessMapper).toEntity(dtoCaptor.capture());
        SignatureProcessDTO process = dtoCaptor.getValue();
        assertEquals(ProcessTypeEnum.SEQUENTIAL, process.getProcessType());
        assertEquals(SignatureProcessStatus.PENDING, process.getStatus());
        assertNotNull(process.getCreationDate());
        assertEquals(request.getExpirationDate(), process.getExpirationDate());
        assertNotNull(process.getDocument());
        assertEquals(request.getFileName(), process.getDocument().getFileName());
        assertEquals(storedFile.toAbsolutePath().normalize().toString(), process.getDocument().getStoragePath());
        assertEquals(PDF_CONTENT.length, process.getDocument().getSize());
        assertEquals(Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-256").digest(PDF_CONTENT)),
                process.getDocument().getHash());
        assertSame(process, process.getDocument().getSignatureProcess());
        assertEquals(2, process.getSignatories().size());
        assertEquals(1, process.getSignatories().get(0).getSignOrder());
        assertEquals(2, process.getSignatories().get(1).getSignOrder());
        assertEquals(SignatoryStatus.PENDING, process.getSignatories().get(0).getSignStatus());
        assertEquals("Ada", process.getSignatories().get(0).getName());
        assertEquals("ada@example.com", process.getSignatories().get(0).getEmail());
        assertSame(process, process.getSignatories().get(0).getSignatureProcess());
        verify(signatureProcessRepository).save(persistedEntity);
    }

    @Test
    void createProcessAcceptsMaximumNumberOfSigners() {
        List<SignerDTO> signers = new ArrayList<>();
        for (int i = 0; i < Constants.MAX_SIGNATORIES; i++) {
            signers.add(signer("Signer " + i, "signer" + i + "@example.com"));
        }
        SignatureProcess entity = new SignatureProcess();
        when(signatureProcessMapper.toEntity(any(SignatureProcessDTO.class))).thenReturn(entity);
        when(signatureProcessRepository.save(entity)).thenReturn(entity);

        service.createProcess(request(signers));

        ArgumentCaptor<SignatureProcessDTO> captor = ArgumentCaptor.forClass(SignatureProcessDTO.class);
        verify(signatureProcessMapper).toEntity(captor.capture());
        assertEquals(Constants.MAX_SIGNATORIES, captor.getValue().getSignatories().size());
        assertEquals(Constants.MAX_SIGNATORIES,
                captor.getValue().getSignatories().get(Constants.MAX_SIGNATORIES - 1).getSignOrder());
    }

    @Test
    void createProcessRejectsEmptySignerList() {
        SignatureProcessException exception = assertThrows(SignatureProcessException.class,
                () -> service.createProcess(request(List.of())));

        assertTrue(exception.getMessage().contains("entre 1 y " + Constants.MAX_SIGNATORIES));
        verifyNoPersistence();
    }

    @Test
    void createProcessRejectsMoreThanMaximumSigners() {
        List<SignerDTO> signers = new ArrayList<>();
        for (int i = 0; i <= Constants.MAX_SIGNATORIES; i++) {
            signers.add(signer("Signer " + i, "signer" + i + "@example.com"));
        }

        assertThrows(SignatureProcessException.class, () -> service.createProcess(request(signers)));

        verifyNoPersistence();
    }

    @Test
    void createProcessRejectsDuplicateEmailsIgnoringCase() {
        SignatureProcessRequest request = request(signers(
                signer("Ada", "Ada@example.com"), signer("Other", "ada@EXAMPLE.com")));

        SignatureProcessException exception = assertThrows(SignatureProcessException.class,
                () -> service.createProcess(request));

        assertEquals("Existen varios firmantes con el mismo email", exception.getMessage());
        verifyNoPersistence();
    }

    @Test
    void createProcessRejectsInvalidBase64Pdf() {
        SignatureProcessRequest request = request(signers(signer("Ada", "ada@example.com")));
        request.setEncodedPDF("not base64!");

        SignatureProcessException exception = assertThrows(SignatureProcessException.class,
                () -> service.createProcess(request));

        assertEquals("Error al decodificar el PDF", exception.getMessage());
        assertTrue(exception.getCause() instanceof IllegalArgumentException);
        verifyNoPersistence();
        assertFalse(Files.exists(storagePath.resolve(request.getFileName())));
    }

    @Test
    void createProcessWrapsDocumentStorageFailure() throws IOException {
        SignatureProcessRequest request = request(signers(signer("Ada", "ada@example.com")));
        Path occupiedPath = storagePath.resolve(request.getFileName());
        Files.createDirectories(occupiedPath);

        SignatureProcessException exception = assertThrows(SignatureProcessException.class,
                () -> service.createProcess(request));

        assertEquals("Error al almacenar el documento", exception.getMessage());
        assertTrue(exception.getCause() instanceof IOException);
        verifyNoPersistence();
    }

    @Test
    void createProcessAcceptsBothSupportedProcessTypes() {
        SignatureProcess entity = new SignatureProcess();
        when(signatureProcessMapper.toEntity(any(SignatureProcessDTO.class))).thenReturn(entity);
        when(signatureProcessRepository.save(entity)).thenReturn(entity);

        SignatureProcessRequest request = request(signers(signer("Ada", "ada@example.com")));
        request.setProcessType(ProcessTypeEnum.PARALLEL);
        service.createProcess(request);

        ArgumentCaptor<SignatureProcessDTO> captor = ArgumentCaptor.forClass(SignatureProcessDTO.class);
        verify(signatureProcessMapper).toEntity(captor.capture());
        assertEquals(ProcessTypeEnum.PARALLEL, captor.getValue().getProcessType());
    }

    private void verifyNoPersistence() {
        verify(signatureProcessMapper, never()).toEntity(any(SignatureProcessDTO.class));
        verify(signatureProcessRepository, never()).save(any(SignatureProcess.class));
    }

    private static SignatureProcessRequest request(List<SignerDTO> signers) {
        return SignatureProcessRequest.builder()
                .encodedPDF(Base64.getEncoder().encodeToString(PDF_CONTENT))
                .fileName("test-document.pdf")
                .signers(signers)
                .processType(ProcessTypeEnum.SEQUENTIAL)
                .expirationDate(LocalDateTime.now().plusDays(2))
                .build();
    }

    private static List<SignerDTO> signers(SignerDTO... signers) {
        return List.of(signers);
    }

    private static SignerDTO signer(String name, String email) {
        return SignerDTO.builder().name(name).email(email).build();
    }
}
