package com.docuten.project.exception;

/**
 * Exception thrown when there is an error during the signing of a document.
 * SignDocumentException
 */
public class SignDocumentException extends RuntimeException {

    public SignDocumentException(String message) {
        super(message);
    }

    public SignDocumentException(String message, Throwable cause) {
        super(message, cause);
    }

}
