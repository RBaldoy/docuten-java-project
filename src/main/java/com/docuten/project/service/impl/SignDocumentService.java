package com.docuten.project.service.impl;

import java.security.KeyPair;

import org.springframework.stereotype.Service;

import com.docuten.project.service.IKeyManagerService;
import com.docuten.project.service.ISignDocumentService;
import com.docuten.project.model.Signatory;
import com.docuten.project.model.SignatoryKey;
import com.docuten.project.model.dto.KeyPairResponseDTO;
import com.docuten.project.model.dto.SignDocumentRequest;
import com.docuten.project.model.dto.SignDocumentResponse;
import com.docuten.project.repository.SignatoryKeyRepository;
import com.docuten.project.repository.SignatoryRepository;
import com.docuten.project.exception.KeyManagerException;
import com.docuten.project.exception.SignDocumentException;

@Service
public class SignDocumentService implements ISignDocumentService {

    private final IKeyManagerService keyManagerService;
    private final SignatoryKeyRepository signatoryKeyRepository;
    private final SignatoryRepository signatoryRepository;

    public SignDocumentService(IKeyManagerService keyManagerService, SignatoryKeyRepository signatoryKeyRepository,
            SignatoryRepository signatoryRepository) {
        this.keyManagerService = keyManagerService;
        this.signatoryKeyRepository = signatoryKeyRepository;
        this.signatoryRepository = signatoryRepository;
    }

    @Override
    public SignDocumentResponse signPdfDocument(Long id, SignDocumentRequest request) throws SignDocumentException {
        // Implement the signing logic here
        return null;
    }

    @Override
    public KeyPairResponseDTO generateKeyPair(Long signatoryId) throws SignDocumentException, KeyManagerException {
        Signatory signatory = signatoryRepository.findById(signatoryId)
                .orElseThrow(() -> new SignDocumentException("Signatory not found with ID: " + signatoryId));
        
        KeyPair keyPair = keyManagerService.generateKeyPair();
        String privateKeyBase64 = keyManagerService.encodePrivateKey(keyPair.getPrivate());
        String publicKeyBase64 = keyManagerService.encodePublicKey(keyPair.getPublic());

        SignatoryKey signatoryKey = new SignatoryKey();
        signatoryKey.setPublicKeyBase64(publicKeyBase64);
        signatoryKey.setPrivateKeyBase64(privateKeyBase64);
        signatoryKey.setSignatory(signatory);
        signatoryKeyRepository.save(signatoryKey);
        signatory.setSignatoryKey(signatoryKey);
        signatoryRepository.save(signatory);

        return KeyPairResponseDTO.builder()
                .privateKeyBase64(privateKeyBase64)
                .publicKeyBase64(publicKeyBase64)
                .build();
    }

}
