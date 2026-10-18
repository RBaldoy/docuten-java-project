package com.docuten.project.service.impl;

import org.springframework.stereotype.Service;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import com.docuten.project.service.IKeyManagerService;
import com.docuten.project.exception.KeyManagerException;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.security.PrivateKey;

@Service
public class KeyManagerService implements IKeyManagerService {

    private static final String RSA_ALGORITHM = "RSA";

    @Override 
    public KeyPair generateKeyPair() throws KeyManagerException {
        KeyPairGenerator keyPairGenerator;
        try {
            keyPairGenerator = KeyPairGenerator.getInstance(RSA_ALGORITHM);
            keyPairGenerator.initialize(2048);
            return keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new KeyManagerException("Failed to generate key pair", e);
        }
    }

    @Override
    public String encodePublicKey(PublicKey publicKey) throws KeyManagerException {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    @Override
    public String encodePrivateKey(PrivateKey privateKey) throws KeyManagerException {
        return Base64.getEncoder().encodeToString(privateKey.getEncoded());
    }

    @Override
    public PublicKey decodePublicKey(String encodedPublicKey) throws KeyManagerException {
        try {
            byte[] decodedKey = Base64.getDecoder().decode(encodedPublicKey);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedKey);
            return KeyFactory.getInstance(RSA_ALGORITHM).generatePublic(keySpec);
        } catch (IllegalArgumentException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new KeyManagerException("Failed to decode public key", e);
        }
    }

    @Override
    public PrivateKey decodePrivateKey(String encodedPrivateKey) throws KeyManagerException {
        try {
            byte[] decodedKey = Base64.getDecoder().decode(encodedPrivateKey);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodedKey);
            return KeyFactory.getInstance(RSA_ALGORITHM).generatePrivate(keySpec);
        } catch (IllegalArgumentException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new KeyManagerException("Failed to decode private key", e);
        }
    }

}
