package com.docuten.project.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

import com.docuten.project.model.enums.SignatureProcessStatusEnum;

/**
 * Represents the document details response.
 * DocumentDetailsResponseDTO
 */
@Getter
@Setter 
@AllArgsConstructor 
@NoArgsConstructor 
@Builder
public class DocumentDetailsResponseDTO {

    private Long id;
    private String fileName;
    private String hash;
    private int size;
    private LocalDateTime creationDate;
    private SignatureProcessStatusEnum processStatus;
    private List<DocumentDetailsSignatoryDTO> signatories;

}
