package com.docuten.project.service;

import java.security.PrivateKey;
import java.security.PublicKey;
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
     * Verifies the given signature against the provided document bytes using the specified public key.
     * @param documentBytes the bytes of the document to be verified
     * @param signatureBytes the signature bytes to be verified
     * @param publicKey the public key used for verification
     * @return true if the signature is valid, false otherwise
     * @throws SignatureJCAException if an error occurs during the verification process
     */
    boolean verify(byte[] documentBytes, byte[] signatureBytes, PublicKey publicKey) throws SignatureJCAException;

}
