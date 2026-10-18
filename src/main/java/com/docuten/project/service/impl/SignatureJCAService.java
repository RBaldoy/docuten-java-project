package com.docuten.project.service.impl;

import com.docuten.project.service.ISignatureJCAService;
import com.docuten.project.exception.SignatureJCAException;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;

import org.springframework.stereotype.Service;

@Service
public class SignatureJCAService implements ISignatureJCAService {

    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";

    @Override
    public byte[] sign(byte[] documentBytes, PrivateKey privateKey) throws SignatureJCAException {
        
        try {
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initSign(privateKey);
            signature.update(documentBytes);
            return signature.sign();
        } catch (NoSuchAlgorithmException e) {
            throw new SignatureJCAException("Signature algorithm not found", e);
        } catch (InvalidKeyException e) {
            throw new SignatureJCAException("Invalid private key", e);
        } catch (SignatureException e) {
            throw new SignatureJCAException("Error signing document", e);
        }
    }

    @Override
    public boolean verify(byte[] documentBytes, byte[] signatureBytes, PublicKey publicKey) throws SignatureJCAException {
        try {
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initVerify(publicKey);
            signature.update(documentBytes);
            return signature.verify(signatureBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new SignatureJCAException("Signature algorithm not found", e);
        } catch (InvalidKeyException e) {
            throw new SignatureJCAException("Invalid public key", e);
        } catch (SignatureException e) {
            throw new SignatureJCAException("Error verifying document", e);
        }
    }

}
