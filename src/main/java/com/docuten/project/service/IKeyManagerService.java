package com.docuten.project.service;

import java.security.KeyPair;
import java.security.PublicKey;
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

}
