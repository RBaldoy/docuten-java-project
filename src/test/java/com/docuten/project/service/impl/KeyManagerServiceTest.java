package com.docuten.project.service.impl;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.docuten.project.exception.KeyManagerException;

class KeyManagerServiceTest {

    private static final KeyManagerService service = new KeyManagerService();
    private static KeyPair keyPair;
    private static X509Certificate certificate;

    @BeforeAll
    static void generateKeys() {
        keyPair = service.generateKeyPair();
        certificate = service.generateCertificate(keyPair);
    }

    @Test
    void generateKeyPairCreates2048BitRsaKeys() {
        assertEquals("RSA", keyPair.getPublic().getAlgorithm());
        assertEquals("RSA", keyPair.getPrivate().getAlgorithm());
        assertEquals(2048, ((java.security.interfaces.RSAPublicKey) keyPair.getPublic()).getModulus().bitLength());
    }

    @Test
    void generateCertificateBindsPublicKeyAndIsSelfSigned() throws Exception {
        assertEquals(keyPair.getPublic(), certificate.getPublicKey());
        assertEquals("CN=LocalCertificate", certificate.getSubjectX500Principal().getName());
        assertEquals(certificate.getSubjectX500Principal(), certificate.getIssuerX500Principal());
        certificate.verify(keyPair.getPublic());
        certificate.checkValidity();
    }

    @Test
    void publicKeyEncodingRoundTrips() {
        String encoded = service.encodePublicKey(keyPair.getPublic());

        PublicKey decoded = service.decodePublicKey(encoded);

        assertArrayEquals(keyPair.getPublic().getEncoded(), decoded.getEncoded());
    }

    @Test
    void privateKeyEncodingRoundTrips() {
        String encoded = service.encodePrivateKey(keyPair.getPrivate());

        PrivateKey decoded = service.decodePrivateKey(encoded);

        assertArrayEquals(keyPair.getPrivate().getEncoded(), decoded.getEncoded());
    }

    @Test
    void certificateEncodingRoundTrips() throws Exception {
        String encoded = service.encodeCertificate(certificate);

        X509Certificate decoded = service.decodeCertificate(encoded);

        assertArrayEquals(certificate.getEncoded(), decoded.getEncoded());
    }

    @Test
    void decodePublicKeyFailsWithInvalidBase64() {
        assertThrows(KeyManagerException.class, () -> service.decodePublicKey("not base64!"));
    }

    @Test
    void decodePublicKeyFailsWithInvalidKeySpec() {
        KeyManagerException exception = assertThrows(KeyManagerException.class,
                () -> service.decodePublicKey("AAAA"));
        assertEquals("Failed to decode public key", exception.getMessage());
    }

    @Test
    void decodePrivateKeyFailsWithInvalidBase64() {
        assertThrows(KeyManagerException.class, () -> service.decodePrivateKey("not base64!"));
    }

    @Test
    void decodePrivateKeyFailsWithInvalidKeySpec() {
        KeyManagerException exception = assertThrows(KeyManagerException.class,
                () -> service.decodePrivateKey("AAAA"));
        assertEquals("Failed to decode private key", exception.getMessage());
    }

    @Test
    void decodeCertificateFailsWithInvalidBase64() {
        assertThrows(KeyManagerException.class, () -> service.decodeCertificate("not base64!"));
    }

    @Test
    void decodeCertificateFailsWithInvalidCertificateBytes() {
        KeyManagerException exception = assertThrows(KeyManagerException.class,
                () -> service.decodeCertificate("AAAA"));
        assertEquals("Failed to decode certificate", exception.getMessage());
    }

    @Test
    void verifyKeyPairAcceptsMatchingPrivateKeyAndCertificate() {
        assertTrue(service.verifyKeyPair(keyPair.getPrivate(), certificate));
    }

    @Test
    void verifyKeyPairRejectsPrivateKeyFromAnotherPair() {
        KeyPair other = service.generateKeyPair();

        assertFalse(service.verifyKeyPair(other.getPrivate(), certificate));
    }

    @Test
    void generatedKeyPairsAreDifferent() {
        KeyPair other = service.generateKeyPair();

        assertNotNull(other);
        assertFalse(java.util.Arrays.equals(keyPair.getPrivate().getEncoded(), other.getPrivate().getEncoded()));
    }
}
