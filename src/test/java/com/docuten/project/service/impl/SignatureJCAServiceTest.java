package com.docuten.project.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.HexFormat;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.docuten.project.exception.SignatureJCAException;

class SignatureJCAServiceTest {

    private static final SignatureJCAService service = new SignatureJCAService();
    private static KeyPair keyPair;
    private static X509Certificate certificate;
    private static byte[] unsignedPdf;

    @BeforeAll
    static void setUp() throws Exception {
        KeyManagerService keyManager = new KeyManagerService();
        keyPair = keyManager.generateKeyPair();
        certificate = keyManager.generateCertificate(keyPair);
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            document.addPage(new PDPage());
            document.save(out);
            unsignedPdf = out.toByteArray();
        }
    }

    @Test
    void signPdfAddsOneSignatureThatVerifies() throws Exception {
        byte[] signed = service.signPDF(unsignedPdf, keyPair.getPrivate(), certificate);

        assertTrue(signed.length > unsignedPdf.length);
        try (PDDocument pdf = Loader.loadPDF(signed)) {
            assertEquals(1, pdf.getSignatureDictionaries().size());
        }
        assertTrue(service.verifyPdfSignatures(signed));
    }

    @Test
    void signPdfCanBeAppliedSeveralTimesAndAllSignaturesVerify() throws Exception {
        byte[] once = service.signPDF(unsignedPdf, keyPair.getPrivate(), certificate);
        byte[] twice = service.signPDF(once, keyPair.getPrivate(), certificate);

        try (PDDocument pdf = Loader.loadPDF(twice)) {
            assertEquals(2, pdf.getSignatureDictionaries().size());
        }
        assertTrue(service.verifyPdfSignatures(twice));
    }

    @Test
    void signPdfFailsWithInvalidPdfBytes() {
        byte[] invalid = "not a pdf".getBytes(StandardCharsets.UTF_8);

        assertThrows(SignatureJCAException.class,
                () -> service.signPDF(invalid, keyPair.getPrivate(), certificate));
    }

    @Test
    void verifyPdfSignaturesReturnsFalseWhenPdfIsNotSigned() {
        assertFalse(service.verifyPdfSignatures(unsignedPdf));
    }

    @Test
    void verifyPdfSignaturesFailsWithInvalidPdfBytes() {
        byte[] invalid = "not a pdf".getBytes(StandardCharsets.UTF_8);

        assertThrows(SignatureJCAException.class, () -> service.verifyPdfSignatures(invalid));
    }

    @Test
    void verifyPdfSignaturesDetectsTamperedSignedContent() {
        byte[] signed = service.signPDF(unsignedPdf, keyPair.getPrivate(), certificate);
        byte[] tampered = signed.clone();
        tampered[10] = (byte) (tampered[10] ^ 0x01);

        boolean valid;
        try {
            valid = service.verifyPdfSignatures(tampered);
        } catch (SignatureJCAException e) {
            valid = false;
        }

        assertFalse(valid);
    }

    @Test
    void verifyDocumentHashAcceptsMatchingHexHashIgnoringCase() throws Exception {
        String hex = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(unsignedPdf));

        assertTrue(service.verifyDocumentHash(unsignedPdf, hex));
        assertTrue(service.verifyDocumentHash(unsignedPdf, hex.toUpperCase()));
    }

    @Test
    void verifyDocumentHashRejectsDifferentHash() {
        assertFalse(service.verifyDocumentHash(unsignedPdf, "0000"));
    }

    @Test
    void calculateHashReturnsBase64Sha256() throws Exception {
        byte[] data = "content".getBytes(StandardCharsets.UTF_8);

        String hash = service.calculateHash(data);

        assertEquals(Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-256").digest(data)), hash);
    }

    @Test
    void calculateHashDiffersForDifferentData() {
        assertNotEquals(service.calculateHash(new byte[] { 1 }), service.calculateHash(new byte[] { 2 }));
    }
}
