package com.docuten.project.service.impl;

import org.springframework.stereotype.Service;

import com.docuten.project.service.ISignDocumentService;
import com.docuten.project.model.dto.SignDocumentRequest;
import com.docuten.project.model.dto.SignDocumentResponse;
import com.docuten.project.exception.SignDocumentException;

@Service
public class SignDocumentService implements ISignDocumentService {

    @Override
    public SignDocumentResponse signPdfDocument(Long id, SignDocumentRequest request) throws SignDocumentException {
        // Implement the signing logic here
        return null;
    }

}
