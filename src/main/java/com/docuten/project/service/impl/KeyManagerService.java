package com.docuten.project.service.impl;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.springframework.stereotype.Service;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import com.docuten.project.service.IKeyManagerService;
import com.docuten.project.exception.KeyManagerException;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import java.security.PrivateKey;

@Service
public class KeyManagerService implements IKeyManagerService {

    private static final String RSA_ALGORITHM = "RSA";
    private static final String X509_CERTIFICATE_TYPE = "X.509";
    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";

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
    public X509Certificate generateCertificate(KeyPair keyPair) throws KeyManagerException {

        try {
            Date startDate = new Date();
            Date endDate = Date.from(Instant.now().plus(365, ChronoUnit.DAYS));
            X500Name subject = new X500Name("CN=LocalCertificate");

            JcaX509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(
                    subject,
                    BigInteger.valueOf(System.currentTimeMillis()),
                    startDate,
                    endDate,
                    subject,
                    keyPair.getPublic());

            ContentSigner signer = new JcaContentSignerBuilder(SIGNATURE_ALGORITHM).build(keyPair.getPrivate());

            return new JcaX509CertificateConverter().getCertificate(builder.build(signer));
        } catch (CertificateException | OperatorCreationException e) {
            throw new KeyManagerException("Failed to generate certificate", e);
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
    public String encodeCertificate(X509Certificate certificate) throws KeyManagerException {
        try {
            return Base64.getEncoder().encodeToString(certificate.getEncoded());
        } catch (CertificateEncodingException e) {
            throw new KeyManagerException("Failed to encode certificate", e);
        }
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

    @Override
    public X509Certificate decodeCertificate(String encodedCertificate) throws KeyManagerException {
        try {
            byte[] certificateBytes = Base64.getDecoder().decode(encodedCertificate);

            return (X509Certificate) CertificateFactory.getInstance(X509_CERTIFICATE_TYPE)
                    .generateCertificate(
                            new ByteArrayInputStream(certificateBytes));
        } catch (IllegalArgumentException | CertificateException e) {
            throw new KeyManagerException("Failed to decode certificate", e);
        }
    }

    @Override
    public boolean verifyKeyPair(PrivateKey privateKey, X509Certificate certificate) throws KeyManagerException {
        try {
            byte[] challenge = UUID.randomUUID().toString()
                    .getBytes(StandardCharsets.UTF_8);

            Signature signer = Signature.getInstance(SIGNATURE_ALGORITHM);
            signer.initSign(privateKey);
            signer.update(challenge);
            byte[] testSignature = signer.sign();

            Signature verifier = Signature.getInstance(SIGNATURE_ALGORITHM);
            verifier.initVerify(certificate.getPublicKey());
            verifier.update(challenge);
            return verifier.verify(testSignature);
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            throw new KeyManagerException("Failed to verify key pair", e);
        }
    }
}
