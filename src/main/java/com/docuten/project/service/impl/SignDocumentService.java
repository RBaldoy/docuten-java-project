package com.docuten.project.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.docuten.project.service.IKeyManagerService;
import com.docuten.project.service.ISignDocumentService;
import com.docuten.project.service.ISignatureJCAService;

import jakarta.transaction.Transactional;

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
import com.docuten.project.exception.KeyManagerException;
import com.docuten.project.exception.SignDocumentException;

@Service
public class SignDocumentService implements ISignDocumentService {

    private final IKeyManagerService keyManagerService;
    private final ISignatureJCAService signatureJCAService;
    private final SignatoryKeyRepository signatoryKeyRepository;
    private final SignatoryRepository signatoryRepository;
    private final DocumentRepository documentRepository;
    private final DocumentVersionRepository documentVersionRepository;
    private final SignatureProcessRepository signatureProcessRepository;

    private Path documentStoragePath;

    public SignDocumentService(IKeyManagerService keyManagerService, ISignatureJCAService signatureJCAService,
            SignatoryKeyRepository signatoryKeyRepository,
            SignatoryRepository signatoryRepository,
            DocumentRepository documentRepository,
            DocumentVersionRepository documentVersionRepository,
            SignatureProcessRepository signatureProcessRepository,
            @Value("${document.storage.path}") String documentStoragePath) {
        this.keyManagerService = keyManagerService;
        this.signatureJCAService = signatureJCAService;
        this.signatoryKeyRepository = signatoryKeyRepository;
        this.signatoryRepository = signatoryRepository;
        this.documentRepository = documentRepository;
        this.documentVersionRepository = documentVersionRepository;
        this.signatureProcessRepository = signatureProcessRepository;
        this.documentStoragePath = Paths.get(documentStoragePath).toAbsolutePath().normalize();
    }

    @Override
    @Transactional 
    public SignDocumentResponse signPdfDocument(Long documentId, Long signatoryId, SignDocumentRequest request)
            throws SignDocumentException {

        Document originalDocument = documentRepository.findById(documentId)
                .orElseThrow(() -> new SignDocumentException("Document not found with ID: " + documentId));

        SignatureProcess signatureProcess = signatureProcessRepository.findById(originalDocument.getSignatureProcess().getId())
                .orElseThrow(() -> new SignDocumentException(
                        "Signature process not found with ID: " + originalDocument.getSignatureProcess().getId()));

        Signatory signatory = signatoryRepository.findById(signatoryId)
                .orElseThrow(() -> new SignDocumentException("Signatory not found with ID: " + signatoryId));

        if (!signatureProcess.getSignatories().contains(signatory)) {
            throw new SignDocumentException("Signatory with ID: " + signatoryId
                    + " is not part of the signature process with ID: " + signatureProcess.getId());
        }

        if (SignatureProcessStatusEnum.COMPLETED.equals(signatureProcess.getStatus())) {
            throw new SignDocumentException(
                    "Signature process with ID: " + signatureProcess.getId() + " is already completed.");
        }

        if (SignatureProcessStatusEnum.EXPIRED.equals(signatureProcess.getStatus())) {
            throw new SignDocumentException(
                    "Signature process with ID: " + signatureProcess.getId() + " is already expired.");
        }

        if (SignatureProcessStatusEnum.REJECTED.equals(signatureProcess.getStatus())) {
            throw new SignDocumentException(
                    "Signature process with ID: " + signatureProcess.getId() + " is already rejected.");
        }

        if (SignatoryStatusEnum.SIGNED.equals(signatory.getSignStatus())) {
            throw new SignDocumentException(
                    "Signatory with ID: " + signatoryId + " has already signed the document with ID: " + documentId);
        }

        if (SignatoryStatusEnum.REJECTED.equals(signatory.getSignStatus())) {
            throw new SignDocumentException(
                    "Signatory with ID: " + signatoryId + " has rejected the document with ID: " + documentId);
        }

        if (ProcessTypeEnum.SEQUENTIAL.equals(signatureProcess.getProcessType()) && !signatory.equals(getCurrentSignatory(signatureProcess))) {
            throw new SignDocumentException(
                    "Signatory with ID: " + signatoryId + " is not the current signatory for the sequential signature process with ID: " + signatureProcess.getId());
        }

        Optional<DocumentVersion> currentDocumentVersionOptional = getCurrentDocumentVersion(originalDocument);

        DocumentVersion newDocumentVersion = null;
        if (currentDocumentVersionOptional.isEmpty()) {
            newDocumentVersion = signPDF(originalDocument, originalDocument.getStoragePath(), 1, signatory, request.getPrivateKeyBase64());
            documentVersionRepository.save(newDocumentVersion);
        } else {
            DocumentVersion currentDocumentVersion = currentDocumentVersionOptional.get();
            newDocumentVersion = signPDF(originalDocument, currentDocumentVersion.getStoragePath(), currentDocumentVersion.getVersionNumber() + 1, signatory, request.getPrivateKeyBase64());
            documentVersionRepository.save(newDocumentVersion);
        }

        updateSignatory(signatory);

        if (signatureProcessIsCompleted(signatureProcess)) {
            signatureProcess.setStatus(SignatureProcessStatusEnum.COMPLETED);
            signatureProcess.setStatusUpdateDate(LocalDateTime.now(ZoneId.systemDefault()));
            signatureProcessRepository.save(signatureProcess);
        } else if (SignatureProcessStatusEnum.PENDING.equals(signatureProcess.getStatus())) {
            signatureProcess.setStatus(SignatureProcessStatusEnum.IN_PROGRESS);
            signatureProcess.setStatusUpdateDate(LocalDateTime.now(ZoneId.systemDefault()));
            signatureProcessRepository.save(signatureProcess);
        }

        originalDocument.getDocumentVersions().add(newDocumentVersion);
        documentRepository.save(originalDocument);

        return SignDocumentResponse.builder()
                .signedDocumentPath(newDocumentVersion.getStoragePath())
                .build();
    }

    @Override
    @Transactional
    public KeyPairResponseDTO generateKeyPair(Long signatoryId) throws SignDocumentException, KeyManagerException {
        Signatory signatory = signatoryRepository.findById(signatoryId)
                .orElseThrow(() -> new SignDocumentException("Signatory not found with ID: " + signatoryId));

        KeyPair keyPair = keyManagerService.generateKeyPair();
        X509Certificate certificate = keyManagerService.generateCertificate(keyPair);
        String privateKeyBase64 = keyManagerService.encodePrivateKey(keyPair.getPrivate());
        String publicKeyBase64 = keyManagerService.encodePublicKey(keyPair.getPublic());
        String certificateBase64 = keyManagerService.encodeCertificate(certificate);

        SignatoryKey signatoryKey = new SignatoryKey();
        signatoryKey.setPublicKeyBase64(publicKeyBase64);
        signatoryKey.setPrivateKeyBase64(privateKeyBase64);
        signatoryKey.setCertificateBase64(certificateBase64);
        signatoryKey.setSignatory(signatory);
        signatoryKeyRepository.save(signatoryKey);
        signatory.setSignatoryKey(signatoryKey);
        signatoryRepository.save(signatory);

        return KeyPairResponseDTO.builder()
                .privateKeyBase64(privateKeyBase64)
                .publicKeyBase64(publicKeyBase64)
                .certificateBase64(certificateBase64)
                .build();
    }

    /**
     * Returns the current signatory for the given sequential signature process.
     * @param signatureProcess the signature process for which to find the current signatory
     * @return the current signatory for the given signature process
     * @throws SignDocumentException if no current signatory is found for the signature process
     */
    private Signatory getCurrentSignatory(SignatureProcess signatureProcess)
            throws SignDocumentException {
        return signatureProcess.getSignatories().stream()
                .filter(signatory -> SignatoryStatusEnum.PENDING.equals(signatory.getSignStatus()))
                .min(Comparator.comparingInt(Signatory::getSignOrder))
                .orElseThrow(() -> new SignDocumentException(
                        "No current signatory found for signature process with ID: "
                                + signatureProcess.getId()));
    }

    /**
     * Checks if the given signature process is completed (all signatories have signed).
     * @param signatureProcess the signature process to check
     * @return true if all signatories have signed, false otherwise
     */
    private boolean signatureProcessIsCompleted(SignatureProcess signatureProcess) {
        return signatureProcess.getSignatories().stream()
                .allMatch(signatory -> SignatoryStatusEnum.SIGNED.equals(signatory.getSignStatus()));
    }

    /**
     * Signs the given PDF document with the provided private key and returns the new document version.
     * @param document the document to be signed
     * @param storagePath the path where the original document is stored
     * @param versionNumber the version number for the new signed document
     * @param signatory the signatory performing the signature
     * @param encodedPrivateKey the Base64-encoded private key of the signatory
     * @return the new document version after signing
     * @throws SignDocumentException if an error occurs during the signing process
     * @throws KeyManagerException if an error occurs while handling the keys
     */
    private DocumentVersion signPDF(Document document, String storagePath, Integer versionNumber, Signatory signatory, String encodedPrivateKey)
            throws SignDocumentException, KeyManagerException {
        
                try {
            SignatoryKey keys = signatoryKeyRepository.findById(signatory.getSignatoryKey().getId())
                    .orElseThrow(() -> new SignDocumentException(
                            "Signatory key not found for signatory with ID: " + signatory.getId()));

            PrivateKey privateKey = keyManagerService.decodePrivateKey(encodedPrivateKey);
            X509Certificate certificate = keyManagerService.decodeCertificate(keys.getCertificateBase64());
            if (!keyManagerService.verifyKeyPair(privateKey, certificate)) {
                throw new SignDocumentException("Private key does not match the certificate's public key");
            }

            Path originalPath = Paths.get(storagePath);

            byte[] originalPdf = Files.readAllBytes(originalPath);

            byte[] signedPdf = signatureJCAService.signPDF(originalPdf, privateKey, certificate);

            Files.createDirectories(documentStoragePath);

            Path outputPath = documentStoragePath.resolve(
                    document.getFileName() + "-signed-V" + versionNumber);

            Files.write(outputPath, signedPdf, StandardOpenOption.CREATE_NEW);

            return DocumentVersion.builder()
            .document(document)
            .versionNumber(versionNumber)
            .storagePath(outputPath.toString())
            .hash(signatureJCAService.calculateHash(signedPdf))
            .creationDate(LocalDateTime.now(ZoneId.systemDefault()))
            .build();

        } catch (IOException e) {
            throw new SignDocumentException("Error signing PDF document", e);
        }
    }

    /**
     * Updates the status and sign date of the given signatory.
     * @param signatory the signatory to update
     */
    private void updateSignatory(Signatory signatory) {
        signatory.setSignStatus(SignatoryStatusEnum.SIGNED);
        signatory.setSignDate(LocalDateTime.now(ZoneId.systemDefault()));
        signatoryRepository.save(signatory);
    }

    /**
     * Retrieves the current (latest) version of the given document, if it exists.
     * @param document the document for which to retrieve the current version
     * @return an Optional containing the current document version, or empty if no versions exist
     */
    private Optional<DocumentVersion> getCurrentDocumentVersion(Document document) {
        return documentVersionRepository.findByDocumentIdOrderByVersionNumberDesc(document.getId())
                .stream()
                .findFirst();
    }
}
