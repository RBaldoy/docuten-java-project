package com.docuten.project.model.dto;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) for the Document entity
 * DocumentDTO
 */
@Getter
@Setter 
@AllArgsConstructor 
@NoArgsConstructor 
@Builder 
public class DocumentDTO {

    private Long id;
    private String fileName;
    private String storagePath;
    private String hash;
    private int size;
    private Date creationDate;
    private SignatureProcessDTO signatureProcess;
}
