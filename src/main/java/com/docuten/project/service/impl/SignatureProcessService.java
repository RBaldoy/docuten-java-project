package com.docuten.project.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.docuten.project.exception.SignatureProcessException;
import com.docuten.project.model.dto.DocumentDTO;
import com.docuten.project.model.dto.SignatoryDTO;
import com.docuten.project.model.dto.SignatureProcessDTO;
import com.docuten.project.model.dto.SignatureProcessRequest;
import com.docuten.project.model.dto.SignerDTO;
import com.docuten.project.model.enums.SignatoryStatus;
import com.docuten.project.model.enums.SignatureProcessStatus;
import com.docuten.project.repository.SignatureProcessRepository;
import com.docuten.project.service.ISignatureProcessService;
import com.docuten.project.service.constants.Constants;
import com.docuten.project.service.mapper.SignatureProcessMapper;

@Service
public class SignatureProcessService implements ISignatureProcessService {

    private final SignatureProcessMapper signatureProcessMapper;
    private final SignatureProcessRepository signatureProcessRepository;
    private final Path documentStoragePath;

    public SignatureProcessService(SignatureProcessMapper signatureProcessMapper,
            SignatureProcessRepository signatureProcessRepository,
            @Value("${document.storage.path}") String documentStoragePath) {
        this.signatureProcessMapper = signatureProcessMapper;
        this.signatureProcessRepository = signatureProcessRepository;
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
                .status(SignatureProcessStatus.PENDING)
                .creationDate(LocalDateTime.now(ZoneId.systemDefault()))
                .expirationDate(request.getExpirationDate())
                .build();

        DocumentDTO documentDTO = DocumentDTO.builder()
                .fileName(request.getFileName())
                .storagePath(documentPath.toString())
                .hash(calculateHash(decodedPDF))
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
                    .signStatus(SignatoryStatus.PENDING)
                    .creationDate(LocalDateTime.now(ZoneId.systemDefault()))
                    .signatureProcess(signatureProcessDTO)
                    .build();
            signatories.add(signatoryDTO);
        }

        signatureProcessDTO.setSignatories(signatories);

        return signatureProcessRepository.save(signatureProcessMapper.toEntity(signatureProcessDTO)).getId();
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

    /**
     * Calculates the SHA-256 hash of the given data.
     *
     * @param data the data to hash
     * @return the Base64-encoded hash
     */
    private String calculateHash(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(data);

            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new SignatureProcessException("Error al calcular el hash del documento", e);
        }
    }

}
