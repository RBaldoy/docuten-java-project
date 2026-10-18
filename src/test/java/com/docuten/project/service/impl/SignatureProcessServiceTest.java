package com.docuten.project.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.docuten.project.exception.SignatureProcessException;
import com.docuten.project.model.Document;
import com.docuten.project.model.DocumentVersion;
import com.docuten.project.model.SignatureProcess;
import com.docuten.project.model.dto.SignatureProcessDTO;
import com.docuten.project.model.dto.SignatureProcessRequest;
import com.docuten.project.model.dto.SignerDTO;
import com.docuten.project.model.enums.ProcessTypeEnum;
import com.docuten.project.model.enums.SignatoryStatusEnum;
import com.docuten.project.model.enums.SignatureProcessStatusEnum;
import com.docuten.project.repository.DocumentVersionRepository;
import com.docuten.project.repository.SignatureProcessRepository;
import com.docuten.project.service.ISignatureJCAService;
import com.docuten.project.service.constants.Constants;
import com.docuten.project.service.mapper.SignatureProcessMapper;

@ExtendWith(MockitoExtension.class)
class SignatureProcessServiceTest {

    private static final String HASH = "document-hash";
    private static final byte[] PDF_CONTENT = "%PDF-1.7 test".getBytes(java.nio.charset.StandardCharsets.UTF_8);

    @TempDir
    Path storagePath;

    @Mock
    private SignatureProcessMapper signatureProcessMapper;

    @Mock
    private SignatureProcessRepository signatureProcessRepository;

    @Mock
    private ISignatureJCAService signatureJCAService;

    @Mock
    private DocumentVersionRepository documentVersionRepository;

    private SignatureProcessService service;

    @BeforeEach
    void setUpService() {
        service = new SignatureProcessService(signatureJCAService, signatureProcessMapper,
                signatureProcessRepository, documentVersionRepository, storagePath.toString());
        lenient().when(signatureJCAService.calculateHash(any(byte[].class))).thenReturn(HASH);
    }

    @Test
    void verifySignatureFailsWhenProcessDoesNotExist() {
        when(signatureProcessRepository.findById(99L)).thenReturn(Optional.empty());

        SignatureProcessException exception = assertThrows(SignatureProcessException.class,
                () -> service.verifySignature(99L));

        assertTrue(exception.getMessage().contains("99"));
    }

    @Test
    void verifySignatureUsesOriginalDocumentWhenThereAreNoVersions() throws IOException {
        Path file = storagePath.resolve("original.pdf");
        Files.write(file, PDF_CONTENT);
        Document document = document(file, "original-hash");
        when(signatureProcessRepository.findById(1L)).thenReturn(Optional.of(processWith(document)));
        when(documentVersionRepository.findByDocumentIdOrderByVersionNumberDesc(10L)).thenReturn(List.of());
        when(signatureJCAService.verifyDocumentHash(PDF_CONTENT, "original-hash")).thenReturn(true);
        when(signatureJCAService.verifyPdfSignatures(PDF_CONTENT)).thenReturn(true);

        assertTrue(service.verifySignature(1L));
    }

    @Test
    void verifySignatureUsesLatestVersionWhenAvailable() throws IOException {
        Path versionFile = storagePath.resolve("signed.pdf");
        byte[] signed = "%PDF signed".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        Files.write(versionFile, signed);
        Document document = document(storagePath.resolve("missing.pdf"), "original-hash");
        DocumentVersion latest = DocumentVersion.builder().versionNumber(2)
                .storagePath(versionFile.toString()).hash("version-hash").build();
        when(signatureProcessRepository.findById(1L)).thenReturn(Optional.of(processWith(document)));
        when(documentVersionRepository.findByDocumentIdOrderByVersionNumberDesc(10L)).thenReturn(List.of(latest));
        when(signatureJCAService.verifyDocumentHash(signed, "version-hash")).thenReturn(true);
        when(signatureJCAService.verifyPdfSignatures(signed)).thenReturn(false);

        assertFalse(service.verifySignature(1L));
    }

    @Test
    void verifySignatureReturnsFalseWithoutCheckingSignaturesWhenHashDiffers() throws IOException {
        Path file = storagePath.resolve("original.pdf");
        Files.write(file, PDF_CONTENT);
        when(signatureProcessRepository.findById(1L))
                .thenReturn(Optional.of(processWith(document(file, "original-hash"))));
        when(documentVersionRepository.findByDocumentIdOrderByVersionNumberDesc(10L)).thenReturn(List.of());
        when(signatureJCAService.verifyDocumentHash(PDF_CONTENT, "original-hash")).thenReturn(false);

        assertFalse(service.verifySignature(1L));
        verify(signatureJCAService, never()).verifyPdfSignatures(any());
    }

    @Test
    void verifySignatureWrapsIOExceptionWhenFileCannotBeRead() {
        Document document = document(storagePath.resolve("missing.pdf"), "hash");
        when(signatureProcessRepository.findById(1L)).thenReturn(Optional.of(processWith(document)));
        when(documentVersionRepository.findByDocumentIdOrderByVersionNumberDesc(10L)).thenReturn(List.of());

        SignatureProcessException exception = assertThrows(SignatureProcessException.class,
                () -> service.verifySignature(1L));

        assertTrue(exception.getCause() instanceof IOException);
    }

    private static Document document(Path path, String hash) {
        Document document = new Document();
        document.setId(10L);
        document.setStoragePath(path.toString());
        document.setHash(hash);
        return document;
    }

    private static SignatureProcess processWith(Document document) {
        SignatureProcess process = new SignatureProcess();
        process.setId(1L);
        process.setDocument(document);
        return process;
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
        assertEquals(SignatureProcessStatusEnum.PENDING, process.getStatus());
        assertNotNull(process.getCreationDate());
        assertEquals(request.getExpirationDate(), process.getExpirationDate());
        assertNotNull(process.getDocument());
        assertEquals(request.getFileName(), process.getDocument().getFileName());
        assertEquals(storedFile.toAbsolutePath().normalize().toString(), process.getDocument().getStoragePath());
        assertEquals(PDF_CONTENT.length, process.getDocument().getSize());
        assertEquals(HASH, process.getDocument().getHash());
        assertSame(process, process.getDocument().getSignatureProcess());
        assertEquals(2, process.getSignatories().size());
        assertEquals(1, process.getSignatories().get(0).getSignOrder());
        assertEquals(2, process.getSignatories().get(1).getSignOrder());
        assertEquals(SignatoryStatusEnum.PENDING, process.getSignatories().get(0).getSignStatus());
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
