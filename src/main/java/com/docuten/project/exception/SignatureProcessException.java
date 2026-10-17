package com.docuten.project.exception;

/**
 * Exception thrown when there is an error in the signature process
 * SignatureProcessException
 */
public class SignatureProcessException extends RuntimeException {
    

    public SignatureProcessException(String message, Throwable cause) {
        super(message, cause);
    }

    public SignatureProcessException(String message) {
        super(message);
    }

}
