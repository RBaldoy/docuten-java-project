package com.docuten.project.exception;

/**
 * Exception thrown when there is an error retrieving document details.
 * DocumentException
 */
public class DocumentException extends RuntimeException {
    
    public DocumentException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public DocumentException(String message) {
        super(message);
    }
}
