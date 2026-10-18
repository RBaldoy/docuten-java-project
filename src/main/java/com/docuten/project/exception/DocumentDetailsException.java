package com.docuten.project.exception;

/**
 * Exception thrown when there is an error retrieving document details.
 * DocumentDetailsException
 */
public class DocumentDetailsException extends RuntimeException {
    
    public DocumentDetailsException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public DocumentDetailsException(String message) {
        super(message);
    }
}
