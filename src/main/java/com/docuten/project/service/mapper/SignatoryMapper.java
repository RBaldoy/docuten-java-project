package com.docuten.project.service.mapper;

import org.springframework.stereotype.Component;

import com.docuten.project.model.Signatory;
import com.docuten.project.model.dto.SignatoryDTO;

import org.springframework.context.annotation.Lazy;

/**
 * Mapper for converting between Signatory and SignatoryDTO
 * SignatoryMapper
 */
@Component
public class SignatoryMapper {

    private final SignatureProcessMapper signatureProcessMapper;

    public SignatoryMapper(@Lazy SignatureProcessMapper signatureProcessMapper) {
        this.signatureProcessMapper = signatureProcessMapper;
    }

    /**
     * Map a Signatory entity to a SignatoryDTO.
     * @param signatory signatory entity
     * @return SignatoryDTO
     */
    public SignatoryDTO toDTO(Signatory signatory) {
        return toDTO(signatory, true);
    }

    SignatoryDTO toDTOWithoutSignatureProcess(Signatory signatory) {
        return toDTO(signatory, false);
    }

    private SignatoryDTO toDTO(Signatory signatory, boolean includeSignatureProcess) {
        if (signatory == null) {
            return null;
        }

        SignatoryDTO signatoryDTO = SignatoryDTO.builder()
                .id(signatory.getId())
                .name(signatory.getName())
                .email(signatory.getEmail())
                .signOrder(signatory.getSignOrder())
                .signStatus(signatory.getSignStatus())
                .signDate(signatory.getSignDate())
                .creationDate(signatory.getCreationDate())
                .build();

        if (includeSignatureProcess && signatory.getSignatureProcess() != null) {
            signatoryDTO.setSignatureProcess(signatureProcessMapper.toDTO(signatory.getSignatureProcess()));
        }
        return signatoryDTO;
    }

    /**
     * Map a SignatoryDTO to a Signatory entity.
     * @param signatoryDTO signatoryDTO
     * @return Signatory entity
     */
    public Signatory toEntity(SignatoryDTO signatoryDTO) {
        return toEntity(signatoryDTO, true);
    }

    Signatory toEntityWithoutSignatureProcess(SignatoryDTO signatoryDTO) {
        return toEntity(signatoryDTO, false);
    }

    private Signatory toEntity(SignatoryDTO signatoryDTO, boolean includeSignatureProcess) {
        if (signatoryDTO == null) {
            return null;
        }

        Signatory signatory = new Signatory();
        signatory.setName(signatoryDTO.getName());
        signatory.setEmail(signatoryDTO.getEmail());
        signatory.setSignOrder(signatoryDTO.getSignOrder());
        signatory.setSignStatus(signatoryDTO.getSignStatus());
        signatory.setSignDate(signatoryDTO.getSignDate());
        signatory.setCreationDate(signatoryDTO.getCreationDate());

        if (includeSignatureProcess && signatoryDTO.getSignatureProcess() != null) {
            signatory.setSignatureProcess(signatureProcessMapper.toEntity(signatoryDTO.getSignatureProcess()));
        }
        return signatory;
    }
}
