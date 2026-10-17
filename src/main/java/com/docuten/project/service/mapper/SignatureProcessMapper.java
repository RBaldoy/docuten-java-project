package com.docuten.project.service.mapper;

import org.springframework.stereotype.Component;

import com.docuten.project.model.SignatureProcess;
import com.docuten.project.model.dto.SignatureProcessDTO;

import lombok.RequiredArgsConstructor;

/**
 * Mapper for converting between SignatureProcess and SignatureProcessDTO
 * SignatureProcessMapper
 */
@Component
@RequiredArgsConstructor 
public class SignatureProcessMapper {

    private final DocumentMapper documentMapper;
    private final SignatoryMapper signatoryMapper;
   
    /**
     * Map a SignatureProcess entity to a SignatureProcessDTO.
     * @param signatureProcess signatureProcess entity
     * @return SignatureProcessDTO
     */
    public SignatureProcessDTO toDTO(SignatureProcess signatureProcess) {
        if (signatureProcess == null) {
            return null;
        }

        SignatureProcessDTO dto = SignatureProcessDTO.builder()
                .id(signatureProcess.getId())
                .processType(signatureProcess.getProcessType())
                .status(signatureProcess.getStatus())
                .creationDate(signatureProcess.getCreationDate())
                .expirationDate(signatureProcess.getExpirationDate())
                .statusUpdateDate(signatureProcess.getStatusUpdateDate())
                .build();
        
        if (signatureProcess.getDocument() != null) {
            dto.setDocument(documentMapper.toDTO(signatureProcess.getDocument()));
        }

        if (signatureProcess.getSignatories() != null) {
            dto.setSignatories(signatureProcess.getSignatories().stream()
                    .map(signatoryMapper::toDTO)
                    .toList());
        }

        return dto;
    }

    /**
     * Map a SignatureProcessDTO to a SignatureProcess entity.
     * @param signatureProcessDTO signatureProcessDTO
     * @return SignatureProcess entity
     */
    public SignatureProcess toEntity(SignatureProcessDTO signatureProcessDTO) {
        if (signatureProcessDTO == null) {
            return null;
        }

        SignatureProcess entity = new SignatureProcess();
        entity.setProcessType(signatureProcessDTO.getProcessType());
        entity.setStatus(signatureProcessDTO.getStatus());
        entity.setCreationDate(signatureProcessDTO.getCreationDate());
        entity.setExpirationDate(signatureProcessDTO.getExpirationDate());
        entity.setStatusUpdateDate(signatureProcessDTO.getStatusUpdateDate());

        if (signatureProcessDTO.getDocument() != null) {
            entity.setDocument(documentMapper.toEntity(signatureProcessDTO.getDocument()));
        }

        if (signatureProcessDTO.getSignatories() != null) {
            entity.setSignatories(signatureProcessDTO.getSignatories().stream()
                    .map(signatoryMapper::toEntity)
                    .toList());
        }

        return entity;
    }


}
