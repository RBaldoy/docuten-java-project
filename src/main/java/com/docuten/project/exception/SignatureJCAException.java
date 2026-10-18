package com.docuten.project.exception;

/**
 * Exception thrown for errors related to JCA signature operations.
 * SignatureJCAException
 */
public class SignatureJCAException extends RuntimeException {
    
    public SignatureJCAException(String message) {
        super(message);
    }

    public SignatureJCAException(String message, Throwable cause) {
        super(message, cause);
    }

}
