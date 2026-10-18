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
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.docuten.project.exception.SignDocumentException;
import com.docuten.project.model.Document;
import com.docuten.project.model.DocumentVersion;
import com.docuten.project.model.Signatory;
import com.docuten.project.model.SignatoryKey;
import com.docuten.project.model.SignatureProcess;
import com.docuten.project.model.dto.KeyPairResponseDTO;
import com.docuten.project.model.dto.SignDocumentRequest;
import com.docuten.project.model.dto.SignDocumentResponse;
import com.docuten.project.model.enums.ProcessTypeEnum;
import com.docuten.project.model.enums.SignatoryStatusEnum;
import com.docuten.project.model.enums.SignatureProcessStatusEnum;
import com.docuten.project.repository.DocumentRepository;
import com.docuten.project.repository.DocumentVersionRepository;
import com.docuten.project.repository.SignatoryKeyRepository;
import com.docuten.project.repository.SignatoryRepository;
import com.docuten.project.repository.SignatureProcessRepository;
import com.docuten.project.service.IKeyManagerService;
import com.docuten.project.service.ISignatureJCAService;

@ExtendWith(MockitoExtension.class)
class SignDocumentServiceTest {

    private static final byte[] ORIGINAL = "original".getBytes();
    private static final byte[] SIGNED = "signed".getBytes();

    @TempDir
    Path storage;

    @Mock
    private IKeyManagerService keyManagerService;
    @Mock
    private ISignatureJCAService signatureJCAService;
    @Mock
    private SignatoryKeyRepository signatoryKeyRepository;
    @Mock
    private SignatoryRepository signatoryRepository;
    @Mock
    private DocumentRepository documentRepository;
    @Mock
    private DocumentVersionRepository documentVersionRepository;
    @Mock
    private SignatureProcessRepository signatureProcessRepository;
    @Mock
    private PrivateKey privateKey;
    @Mock
    private PublicKey publicKey;
    @Mock
    private X509Certificate x509Certificate;

    private SignDocumentService service;
    private Path originalFile;
    private Document document;
    private SignatureProcess process;
    private Signatory first;
    private Signatory second;
    private final SignDocumentRequest request = SignDocumentRequest.builder().privateKeyBase64("pk").build();

    @BeforeEach
    void setUp() throws IOException {
        service = new SignDocumentService(keyManagerService, signatureJCAService, signatoryKeyRepository,
                signatoryRepository, documentRepository, documentVersionRepository, signatureProcessRepository,
                storage.resolve("out").toString());

        originalFile = storage.resolve("original.pdf");
        Files.write(originalFile, ORIGINAL);

        first = signatory(11L, 1, 101L);
        second = signatory(12L, 2, 102L);
        process = new SignatureProcess();
        process.setId(5L);
        process.setProcessType(ProcessTypeEnum.SEQUENTIAL);
        process.setStatus(SignatureProcessStatusEnum.PENDING);
        process.setSignatories(List.of(first, second));
        document = new Document();
        document.setId(1L);
        document.setFileName("a.pdf");
        document.setStoragePath(originalFile.toString());
        document.setDocumentVersions(new ArrayList<>());
        document.setSignatureProcess(process);
        process.setDocument(document);
    }

    @Test
    void signPdfDocumentSignsFirstVersionAndMovesProcessToInProgress() throws IOException {
        stubLookups(first);
        stubSigning(first, ORIGINAL);
        when(documentVersionRepository.findByDocumentIdOrderByVersionNumberDesc(1L)).thenReturn(List.of());

        SignDocumentResponse response = service.signPdfDocument(1L, 11L, request);

        Path expected = storage.resolve("out").resolve("a.pdf-signed-V1").toAbsolutePath().normalize();
        assertEquals(expected.toString(), response.getSignedDocumentPath());
        assertTrue(Arrays.equals(SIGNED, Files.readAllBytes(expected)));
        assertEquals(SignatoryStatusEnum.SIGNED, first.getSignStatus());
        assertNotNull(first.getSignDate());
        assertEquals(SignatureProcessStatusEnum.IN_PROGRESS, process.getStatus());
        assertNotNull(process.getStatusUpdateDate());

        ArgumentCaptor<DocumentVersion> captor = ArgumentCaptor.forClass(DocumentVersion.class);
        verify(documentVersionRepository).save(captor.capture());
        assertEquals(1, captor.getValue().getVersionNumber());
        assertEquals("signed-hash", captor.getValue().getHash());
        assertSame(document, captor.getValue().getDocument());
        assertEquals(List.of(captor.getValue()), document.getDocumentVersions());
        verify(signatoryRepository).save(first);
        verify(signatureProcessRepository).save(process);
        verify(documentRepository).save(document);
    }

    @Test
    void signPdfDocumentCompletesProcessAndSignsLatestVersionWhenLastSignatorySigns() throws IOException {
        first.setSignStatus(SignatoryStatusEnum.SIGNED);
        Path versionFile = storage.resolve("v1.pdf");
        Files.write(versionFile, "v1".getBytes());
        DocumentVersion latest = DocumentVersion.builder().versionNumber(1).storagePath(versionFile.toString())
                .build();
        process.setStatus(SignatureProcessStatusEnum.IN_PROGRESS);
        stubLookups(second);
        stubSigning(second, "v1".getBytes());
        when(documentVersionRepository.findByDocumentIdOrderByVersionNumberDesc(1L)).thenReturn(List.of(latest));

        SignDocumentResponse response = service.signPdfDocument(1L, 12L, request);

        assertTrue(response.getSignedDocumentPath().endsWith("a.pdf-signed-V2"));
        assertEquals(SignatureProcessStatusEnum.COMPLETED, process.getStatus());
        verify(signatureProcessRepository).save(process);
    }

    @Test
    void signPdfDocumentInParallelProcessAllowsAnySignatory() throws IOException {
        process.setProcessType(ProcessTypeEnum.PARALLEL);
        stubLookups(second);
        stubSigning(second, ORIGINAL);
        when(documentVersionRepository.findByDocumentIdOrderByVersionNumberDesc(1L)).thenReturn(List.of());

        service.signPdfDocument(1L, 12L, request);

        assertEquals(SignatoryStatusEnum.SIGNED, second.getSignStatus());
        assertEquals(SignatureProcessStatusEnum.IN_PROGRESS, process.getStatus());
    }

    @Test
    void signPdfDocumentKeepsInProgressStatusWithoutSavingProcessWhenNotLastSignatory() throws IOException {
        process.setProcessType(ProcessTypeEnum.PARALLEL);
        process.setStatus(SignatureProcessStatusEnum.IN_PROGRESS);
        stubLookups(first);
        stubSigning(first, ORIGINAL);
        when(documentVersionRepository.findByDocumentIdOrderByVersionNumberDesc(1L)).thenReturn(List.of());

        service.signPdfDocument(1L, 11L, request);

        assertEquals(SignatureProcessStatusEnum.IN_PROGRESS, process.getStatus());
        verify(signatureProcessRepository, never()).save(any(SignatureProcess.class));
    }

    @Test
    void signPdfDocumentFailsWhenDocumentNotFound() {
        when(documentRepository.findById(1L)).thenReturn(Optional.empty());

        assertFailure("Document not found with ID: 1", 11L);
    }

    @Test
    void signPdfDocumentFailsWhenProcessNotFound() {
        when(documentRepository.findById(1L)).thenReturn(Optional.of(document));
        when(signatureProcessRepository.findById(5L)).thenReturn(Optional.empty());

        assertFailure("Signature process not found with ID: 5", 11L);
    }

    @Test
    void signPdfDocumentFailsWhenSignatoryNotFound() {
        when(documentRepository.findById(1L)).thenReturn(Optional.of(document));
        when(signatureProcessRepository.findById(5L)).thenReturn(Optional.of(process));
        when(signatoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertFailure("Signatory not found with ID: 99", 99L);
    }

    @Test
    void signPdfDocumentFailsWhenSignatoryBelongsToAnotherProcess() {
        Signatory stranger = signatory(99L, 1, 199L);
        when(documentRepository.findById(1L)).thenReturn(Optional.of(document));
        when(signatureProcessRepository.findById(5L)).thenReturn(Optional.of(process));
        when(signatoryRepository.findById(99L)).thenReturn(Optional.of(stranger));

        assertFailure("is not part of the signature process", 99L);
    }

    @Test
    void signPdfDocumentFailsWhenProcessIsCompleted() {
        process.setStatus(SignatureProcessStatusEnum.COMPLETED);
        stubLookups(first);

        assertFailure("already completed", 11L);
    }

    @Test
    void signPdfDocumentFailsWhenProcessIsExpired() {
        process.setStatus(SignatureProcessStatusEnum.EXPIRED);
        stubLookups(first);

        assertFailure("already expired", 11L);
    }

    @Test
    void signPdfDocumentFailsWhenProcessIsRejected() {
        process.setStatus(SignatureProcessStatusEnum.REJECTED);
        stubLookups(first);

        assertFailure("already rejected", 11L);
    }

    @Test
    void signPdfDocumentFailsWhenSignatoryAlreadySigned() {
        first.setSignStatus(SignatoryStatusEnum.SIGNED);
        stubLookups(first);

        assertFailure("has already signed", 11L);
    }

    @Test
    void signPdfDocumentFailsWhenSignatoryRejectedTheDocument() {
        first.setSignStatus(SignatoryStatusEnum.REJECTED);
        stubLookups(first);

        assertFailure("has rejected", 11L);
    }

    @Test
    void signPdfDocumentFailsWhenItIsNotTheSignatorysTurnInSequentialProcess() {
        stubLookups(second);

        assertFailure("is not the current signatory", 12L);
    }

    @Test
    void signPdfDocumentFailsWhenSignatoryKeyIsMissing() {
        stubLookups(first);
        when(documentVersionRepository.findByDocumentIdOrderByVersionNumberDesc(1L)).thenReturn(List.of());
        when(signatoryKeyRepository.findById(101L)).thenReturn(Optional.empty());

        assertFailure("Signatory key not found", 11L);
        verify(documentVersionRepository, never()).save(any(DocumentVersion.class));
    }

    @Test
    void signPdfDocumentFailsWhenPrivateKeyDoesNotMatchCertificate() {
        stubLookups(first);
        when(documentVersionRepository.findByDocumentIdOrderByVersionNumberDesc(1L)).thenReturn(List.of());
        when(signatoryKeyRepository.findById(101L)).thenReturn(Optional.of(first.getSignatoryKey()));
        when(keyManagerService.decodePrivateKey("pk")).thenReturn(privateKey);
        when(keyManagerService.decodeCertificate("cert-101")).thenReturn(x509Certificate);
        when(keyManagerService.verifyKeyPair(privateKey, x509Certificate)).thenReturn(false);

        assertFailure("Private key does not match", 11L);
        assertEquals(SignatoryStatusEnum.PENDING, first.getSignStatus());
        verify(signatoryRepository, never()).save(any(Signatory.class));
    }

    @Test
    void signPdfDocumentWrapsIOExceptionWhenOriginalFileCannotBeRead() throws IOException {
        Files.delete(originalFile);
        stubLookups(first);
        when(documentVersionRepository.findByDocumentIdOrderByVersionNumberDesc(1L)).thenReturn(List.of());
        when(signatoryKeyRepository.findById(101L)).thenReturn(Optional.of(first.getSignatoryKey()));
        when(keyManagerService.decodePrivateKey("pk")).thenReturn(privateKey);
        when(keyManagerService.decodeCertificate("cert-101")).thenReturn(x509Certificate);
        when(keyManagerService.verifyKeyPair(privateKey, x509Certificate)).thenReturn(true);

        SignDocumentException exception = assertThrows(SignDocumentException.class,
                () -> service.signPdfDocument(1L, 11L, request));

        assertEquals("Error signing PDF document", exception.getMessage());
        assertTrue(exception.getCause() instanceof IOException);
        assertFalse(first.getSignStatus() == SignatoryStatusEnum.SIGNED);
    }

    @Test
    void generateKeyPairStoresEncodedKeysAndReturnsThem() {
        KeyPair keyPair = new KeyPair(publicKey, privateKey);
        when(signatoryRepository.findById(11L)).thenReturn(Optional.of(first));
        when(keyManagerService.generateKeyPair()).thenReturn(keyPair);
        when(keyManagerService.generateCertificate(keyPair)).thenReturn(x509Certificate);
        when(keyManagerService.encodePrivateKey(privateKey)).thenReturn("private");
        when(keyManagerService.encodePublicKey(publicKey)).thenReturn("public");
        when(keyManagerService.encodeCertificate(x509Certificate)).thenReturn("certificate");

        KeyPairResponseDTO response = service.generateKeyPair(11L);

        assertEquals("private", response.getPrivateKeyBase64());
        assertEquals("public", response.getPublicKeyBase64());
        assertEquals("certificate", response.getCertificateBase64());
        ArgumentCaptor<SignatoryKey> captor = ArgumentCaptor.forClass(SignatoryKey.class);
        verify(signatoryKeyRepository).save(captor.capture());
        assertEquals("private", captor.getValue().getPrivateKeyBase64());
        assertEquals("public", captor.getValue().getPublicKeyBase64());
        assertEquals("certificate", captor.getValue().getCertificateBase64());
        assertSame(first, captor.getValue().getSignatory());
        assertSame(captor.getValue(), first.getSignatoryKey());
        verify(signatoryRepository).save(first);
    }

    @Test
    void generateKeyPairFailsWhenSignatoryNotFound() {
        when(signatoryRepository.findById(99L)).thenReturn(Optional.empty());

        SignDocumentException exception = assertThrows(SignDocumentException.class,
                () -> service.generateKeyPair(99L));

        assertEquals("Signatory not found with ID: 99", exception.getMessage());
        verify(signatoryKeyRepository, never()).save(any(SignatoryKey.class));
    }

    private void assertFailure(String expectedMessagePart, Long signatoryId) {
        SignDocumentException exception = assertThrows(SignDocumentException.class,
                () -> service.signPdfDocument(1L, signatoryId, request));
        assertTrue(exception.getMessage().contains(expectedMessagePart), exception.getMessage());
    }

    private void stubLookups(Signatory signatory) {
        when(documentRepository.findById(1L)).thenReturn(Optional.of(document));
        when(signatureProcessRepository.findById(5L)).thenReturn(Optional.of(process));
        when(signatoryRepository.findById(signatory.getId())).thenReturn(Optional.of(signatory));
    }

    private void stubSigning(Signatory signatory, byte[] inputBytes) {
        when(signatoryKeyRepository.findById(signatory.getSignatoryKey().getId()))
                .thenReturn(Optional.of(signatory.getSignatoryKey()));
        when(keyManagerService.decodePrivateKey("pk")).thenReturn(privateKey);
        when(keyManagerService.decodeCertificate("cert-" + signatory.getSignatoryKey().getId()))
                .thenReturn(x509Certificate);
        when(keyManagerService.verifyKeyPair(privateKey, x509Certificate)).thenReturn(true);
        when(signatureJCAService.signPDF(inputBytes, privateKey, x509Certificate)).thenReturn(SIGNED);
        when(signatureJCAService.calculateHash(SIGNED)).thenReturn("signed-hash");
    }

    private static Signatory signatory(Long id, int order, Long keyId) {
        SignatoryKey key = new SignatoryKey();
        key.setId(keyId);
        key.setCertificateBase64("cert-" + keyId);
        Signatory signatory = new Signatory();
        signatory.setId(id);
        signatory.setSignOrder(order);
        signatory.setSignStatus(SignatoryStatusEnum.PENDING);
        signatory.setSignatoryKey(key);
        return signatory;
    }
}
