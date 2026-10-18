package com.docuten.project.model.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response object containing the path to the signed document.
 * SignDocumentResponse
 */
@Getter
@Setter 
@AllArgsConstructor 
@NoArgsConstructor 
@Builder
public class SignDocumentResponse {

    private String signedDocumentPath;
}
