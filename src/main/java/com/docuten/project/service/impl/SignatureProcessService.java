package com.docuten.project.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.docuten.project.exception.SignatureProcessException;
import com.docuten.project.model.Document;
import com.docuten.project.model.DocumentVersion;
import com.docuten.project.model.SignatureProcess;
import com.docuten.project.model.dto.DocumentDTO;
import com.docuten.project.model.dto.SignatoryDTO;
import com.docuten.project.model.dto.SignatureProcessDTO;
import com.docuten.project.model.dto.SignatureProcessRequest;
import com.docuten.project.model.dto.SignerDTO;
import com.docuten.project.model.enums.SignatoryStatusEnum;
import com.docuten.project.model.enums.SignatureProcessStatusEnum;
import com.docuten.project.repository.DocumentVersionRepository;
import com.docuten.project.repository.SignatureProcessRepository;
import com.docuten.project.service.ISignatureJCAService;
import com.docuten.project.service.ISignatureProcessService;
import com.docuten.project.service.constants.Constants;
import com.docuten.project.service.mapper.SignatureProcessMapper;

@Service
public class SignatureProcessService implements ISignatureProcessService {

    private final ISignatureJCAService signatureJCAService;
    private final SignatureProcessMapper signatureProcessMapper;
    private final SignatureProcessRepository signatureProcessRepository;
    private final DocumentVersionRepository documentVersionRepository;
    private final Path documentStoragePath;

    public SignatureProcessService(ISignatureJCAService signatureJCAService,
            SignatureProcessMapper signatureProcessMapper,
            SignatureProcessRepository signatureProcessRepository,
            DocumentVersionRepository documentVersionRepository,
            @Value("${document.storage.path}") String documentStoragePath) {
        this.signatureJCAService = signatureJCAService;
        this.signatureProcessMapper = signatureProcessMapper;
        this.signatureProcessRepository = signatureProcessRepository;
        this.documentVersionRepository = documentVersionRepository;
        this.documentStoragePath = Paths.get(documentStoragePath).toAbsolutePath().normalize();
    }

    @Override
    @Transactional
    public Long createProcess(SignatureProcessRequest request) throws SignatureProcessException {

        if (request.getSigners().isEmpty() || request.getSigners().size() > Constants.MAX_SIGNATORIES) {
            throw new SignatureProcessException(
                    "La lista de firmantes debe contener entre 1 y " + Constants.MAX_SIGNATORIES + " firmantes");
        }

        if (!request.getProcessType().getProcessTypes().contains(request.getProcessType().name())) {
            throw new SignatureProcessException(
                    "El tipo de proceso debe: " + String.join(", ", request.getProcessType().getProcessTypes()));
        }

        List<SignerDTO> requestSigners = request.getSigners();
        long numUniqueEmails = requestSigners.stream().map(signer -> signer.getEmail().toLowerCase()).distinct()
                .count();

        if (numUniqueEmails != requestSigners.size()) {
            throw new SignatureProcessException("Existen varios firmantes con el mismo email");
        }

        byte[] decodedPDF = getDecodedPDF(request.getEncodedPDF());
        Path documentPath = storeDocument(decodedPDF, request.getFileName());

        SignatureProcessDTO signatureProcessDTO = SignatureProcessDTO.builder()
                .processType(request.getProcessType())
                .status(SignatureProcessStatusEnum.PENDING)
                .creationDate(LocalDateTime.now(ZoneId.systemDefault()))
                .expirationDate(request.getExpirationDate())
                .build();

        DocumentDTO documentDTO = DocumentDTO.builder()
                .fileName(request.getFileName())
                .storagePath(documentPath.toString())
                .hash(signatureJCAService.calculateHash(decodedPDF))
                .size(decodedPDF.length)
                .creationDate(LocalDateTime.now(ZoneId.systemDefault()))
                .signatureProcess(signatureProcessDTO)
                .build();

        signatureProcessDTO.setDocument(documentDTO);

        List<SignatoryDTO> signatories = new ArrayList<>();
        for (int i = 0; i < requestSigners.size(); i++) {
            SignatoryDTO signatoryDTO = SignatoryDTO.builder()
                    .email(requestSigners.get(i).getEmail())
                    .name(requestSigners.get(i).getName())
                    .signOrder(i + 1)
                    .signStatus(SignatoryStatusEnum.PENDING)
                    .creationDate(LocalDateTime.now(ZoneId.systemDefault()))
                    .signatureProcess(signatureProcessDTO)
                    .build();
            signatories.add(signatoryDTO);
        }

        signatureProcessDTO.setSignatories(signatories);

        return signatureProcessRepository.save(signatureProcessMapper.toEntity(signatureProcessDTO)).getId();
    }

    @Override
    public boolean verifySignature(Long processId) throws SignatureProcessException {
        
        SignatureProcess signatureProcess = signatureProcessRepository.findById(processId)
                .orElseThrow(() -> new SignatureProcessException("Signature process not found for ID: " + processId));

        Document document = signatureProcess.getDocument();

        Optional<DocumentVersion> latestDocumentVersion = documentVersionRepository.findByDocumentIdOrderByVersionNumberDesc(document.getId())
                .stream()
                .findFirst();
        
        String storagePath = null;
        String expectedHash = null;
        if (latestDocumentVersion.isEmpty()) {
            storagePath = document.getStoragePath();
            expectedHash = document.getHash();
        } else {
            storagePath = latestDocumentVersion.get().getStoragePath();
            expectedHash = latestDocumentVersion.get().getHash();
        }

        try {        
            Path originalPath = Paths.get(storagePath);
            byte[] documentBytes = Files.readAllBytes(originalPath);

            return signatureJCAService.verifyDocumentHash(documentBytes, expectedHash) && signatureJCAService.verifyPdfSignatures(documentBytes);
        } catch (IOException e) {
            throw new SignatureProcessException("Error reading the document from storage path", e);
        }
    }

    /**
     * Decodes a Base64-encoded PDF string.
     *
     * @param encodedPDF the Base64-encoded PDF string
     * @return the decoded PDF bytes
     * @throws SignatureProcessException if an error occurs during decoding
     */
    private byte[] getDecodedPDF(String encodedPDF) throws SignatureProcessException {
        try {
            return Base64.getDecoder().decode(encodedPDF);
        } catch (IllegalArgumentException e) {
            throw new SignatureProcessException("Error al decodificar el PDF", e);
        }
    }

    /**
     * Stores a document in the file system.
     *
     * @param document the document bytes
     * @param fileName the file name
     * @return the path where the document is stored
     * @throws SignatureProcessException if an error occurs during storage
     */
    private Path storeDocument(byte[] document, String fileName) throws SignatureProcessException {
        try {
            Files.createDirectories(documentStoragePath);
            Path filePath = documentStoragePath.resolve(fileName);
            return Files.write(filePath, document, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
        } catch (IOException e) {
            throw new SignatureProcessException("Error al almacenar el documento", e);
        }
    }

}
