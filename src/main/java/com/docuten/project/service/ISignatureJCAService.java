package com.docuten.project.service;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import com.docuten.project.exception.SignatureJCAException;

/**
 * Service interface for signing and verifying documents using Java Cryptography Architecture (JCA).
 * ISignatureJCAService
 */
public interface ISignatureJCAService {

    /**
     * Signs the given PDF document bytes using the provided private key and certificate.
     * @param documentBytes the bytes of the document to be signed
     * @param privateKey the private key used for signing
     * @param certificate the X.509 certificate associated with the private key
     * @return the generated signature bytes
     * @throws SignatureJCAException if an error occurs during the signing process
     */
    byte[] signPDF(byte[] documentBytes, PrivateKey privateKey, X509Certificate certificate) throws SignatureJCAException;

    
    /**
     * Verifies the hash of the given PDF document against the expected hash. (Document not modified)
     *
     * @param pdfBytes the bytes of the PDF document
     * @param expectedHash the expected Base64-encoded hash
     * @return true if the hash matches, false otherwise
     * @throws SignatureJCAException if an error occurs during the verification process
     */
    boolean verifyDocumentHash(byte[] pdfBytes, String expectedHash) throws SignatureJCAException;

    /**
     * Verifies the signatures of the given PDF document.
     * @param pdfBytes the bytes of the PDF document to verify
     * @return true if all signatures are valid, false otherwise
     * @throws SignatureJCAException if an error occurs during the verification process
     */
    boolean verifyPdfSignatures(byte[] pdfBytes) throws SignatureJCAException;

    /**
     * Calculates the SHA-256 hash of the given data.
     *
     * @param data the data to hash
     * @return the Base64-encoded hash
     */
    String calculateHash(byte[] data);
}
