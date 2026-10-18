package com.docuten.project.service;

import java.security.KeyPair;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.security.PrivateKey;

import com.docuten.project.exception.KeyManagerException;

/**
 * Service interface for managing RSA key pairs, including generation, encoding, and decoding.
 * IKeyManagerService
 */
public interface IKeyManagerService {

    /**
     * Generates a new RSA key pair.
     * @return the generated RSA key pair
     * @throws KeyManagerException if the key pair generation fails
     */
    KeyPair generateKeyPair() throws KeyManagerException;

    /**
     * Generates a self-signed X.509 certificate for the given RSA key pair.
     * @param keyPair the RSA key pair for which to generate the certificate
     * @return the generated X.509 certificate
     * @throws KeyManagerException if the certificate generation fails
     */
    X509Certificate generateCertificate(KeyPair keyPair) throws KeyManagerException;

    /**
     * Encodes the given RSA public key into a base 64 string.
     * @param publicKey the RSA public key to encode
     * @return the string representation of the encoded public key
     * @throws KeyManagerException if the encoding fails
     */
    String encodePublicKey(PublicKey publicKey) throws KeyManagerException;

    /**
     * Encodes the given RSA private key into a base64 string.
     * @param privateKey the RSA private key to encode
     * @return the string representation of the encoded private key
     * @throws KeyManagerException if the encoding fails
     */
    String encodePrivateKey(PrivateKey privateKey) throws KeyManagerException;

    /**
     * Encodes the given X.509 certificate into a base64 string.
     * @param certificate the X.509 certificate to encode
     * @return the string representation of the encoded certificate
     * @throws KeyManagerException if the encoding fails
     */
    String encodeCertificate(X509Certificate certificate) throws KeyManagerException;

    /**
     * Decodes the given base64 encoded RSA public key
     * @param encodedPublicKey the base64 encoded RSA public key to decode
     * @return the decoded RSA public key
     * @throws KeyManagerException if the decoding fails
     */
    PublicKey decodePublicKey(String encodedPublicKey) throws KeyManagerException;

    /**
     * Decodes the given base64 encoded RSA private key
     * @param encodedPrivateKey the base64 encoded RSA private key to decode
     * @return the decoded RSA private key
     * @throws KeyManagerException if the decoding fails
     */
    PrivateKey decodePrivateKey(String encodedPrivateKey) throws KeyManagerException;
    
    /**
     * Decodes the given base64 encoded X.509 certificate.
     * @param encodedCertificate the base64 encoded X.509 certificate to decode
     * @return the decoded X.509 certificate
     * @throws KeyManagerException if the decoding fails
     */
    X509Certificate decodeCertificate(String encodedCertificate) throws KeyManagerException;

    /**
     * Verifies that the given private key corresponds to the public key in the given X.509 certificate.
     * @param privateKey the RSA private key to verify
     * @param certificate the X.509 certificate containing the corresponding public key
     * @return true if the private key matches the public key in the certificate, false otherwise
     * @throws KeyManagerException if the verification process encounters an error
     */
    boolean verifyKeyPair(PrivateKey privateKey, X509Certificate certificate) throws KeyManagerException;

}
