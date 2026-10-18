package com.docuten.project.service.impl;

import com.docuten.project.service.ISignatureJCAService;
import com.docuten.project.exception.SignatureJCAException;

import jakarta.transaction.Transactional;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Calendar;
import java.util.Collection;
import java.util.HexFormat;
import java.util.List;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.util.Store;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;

import org.apache.pdfbox.Loader;
import org.springframework.stereotype.Service;

@Service
public class SignatureJCAService implements ISignatureJCAService {

    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";
    private static final String HASH_ALGORITHM = "SHA-256";

    @Override
    @Transactional
    public byte[] signPDF(byte[] documentBytes, PrivateKey privateKey, X509Certificate certificate) throws SignatureJCAException {
        
        try (PDDocument pdf = Loader.loadPDF(documentBytes); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            PDSignature signature = new PDSignature();
            signature.setName(certificate.getSubjectX500Principal().getName());
            signature.setSignDate(Calendar.getInstance());

            pdf.addSignature(signature, content -> createCMSSignature(content, privateKey, certificate));

            pdf.saveIncremental(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new SignatureJCAException("Error processing PDF document", e);
        }
    }

    @Override
    public boolean verifyDocumentHash(byte[] pdfBytes, String expectedHash) throws SignatureJCAException {
        try {
            byte[] actualHash = MessageDigest.getInstance(HASH_ALGORITHM).digest(pdfBytes);
            String actualHashHex = HexFormat.of().formatHex(actualHash);
            return MessageDigest.isEqual(actualHashHex.getBytes(StandardCharsets.US_ASCII), expectedHash.toLowerCase().getBytes(StandardCharsets.US_ASCII));
        } catch (NoSuchAlgorithmException e) {
            throw new SignatureJCAException("Error verifying document hash", e);
        }
    }

    @Override
    public boolean verifyPdfSignatures(byte[] pdfBytes) throws SignatureJCAException {
        try (PDDocument pdf = Loader.loadPDF(pdfBytes)) {
            List<PDSignature> signatures = pdf.getSignatureDictionaries();
            if (signatures.isEmpty()) {
                return false;
            }

            for (PDSignature signature : signatures) {
                if (!verifySignature(signature, pdfBytes)) {
                    return false;
                }
            }
            return true;
        } catch (IOException | CMSException | CertificateException | OperatorCreationException e) {
            throw new SignatureJCAException("Error verifying PDF signatures", e);
        }
    }

    @Override
    public String calculateHash(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] hashBytes = digest.digest(data);

            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new SignatureJCAException("Error al calcular el hash del documento", e);
        }
    }

    /**
     * Verifies the signature of a PDF document.
     * @param signature the PDF signature to be verified
     * @param pdfBytes the byte array of the PDF document
     * @return true if the signature is valid, false otherwise
     * @throws IOException if an I/O error occurs while reading the PDF
     * @throws CMSException if an error occurs while processing the CMS data
     * @throws CertificateException if an error occurs while processing the certificate
     * @throws OperatorCreationException if an error occurs while creating the signer info verifier
     * @throws CMSException if an error occurs while processing the CMS data
     */
    private boolean verifySignature(PDSignature signature, byte[] pdfBytes)
            throws IOException, CMSException, CertificateException, OperatorCreationException {
        if (!hasValidByteRange(signature.getByteRange(), pdfBytes.length)) {
            return false;
        }

        byte[] signatureContents = signature.getContents(pdfBytes);
        if (signatureContents.length == 0) {
            return false;
        }

        CMSSignedData cms = new CMSSignedData(
                new CMSProcessableByteArray(signature.getSignedContent(pdfBytes)),
                new ByteArrayInputStream(signatureContents));
        SignerInformationStore signerInfos = cms.getSignerInfos();
        if (signerInfos.size() == 0) {
            return false;
        }

        Store<X509CertificateHolder> certificates = cms.getCertificates();
        for (SignerInformation signerInfo : signerInfos.getSigners()) {
            if (!hasVerifiedSigner(signerInfo, certificates)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the byte range of a PDF signature is valid.
     * @param byteRange the byte range array from the PDF signature
     * @param pdfLength the total length of the PDF document
     * @return true if the byte range is valid, false otherwise
     */
    private boolean hasValidByteRange(int[] byteRange, int pdfLength) {
        return byteRange != null && byteRange.length == 4 && byteRange[0] == 0
                && byteRange[1] >= 0 && byteRange[2] >= byteRange[1] && byteRange[3] >= 0
                && (long) byteRange[2] + byteRange[3] <= pdfLength;
    }

    /**
     * Checks if the given signer information has been verified against the provided certificates.
     * @param signerInfo the signer information to be verified
     * @param certificates the store of X.509 certificates to verify against
     * @return true if the signer is verified, false otherwise
     * @throws CertificateException if an error occurs while processing the certificate
     * @throws OperatorCreationException if an error occurs while creating the signer info verifier
     * @throws CMSException if an error occurs while processing the CMS data
     */
    private boolean hasVerifiedSigner(SignerInformation signerInfo, Store<X509CertificateHolder> certificates)
            throws CertificateException, OperatorCreationException, CMSException {
        Collection<X509CertificateHolder> matches = certificates.getMatches(signerInfo.getSID());
        for (X509CertificateHolder holder : matches) {
            X509Certificate certificate = new JcaX509CertificateConverter().getCertificate(holder);
            if (signerInfo.verify(new JcaSimpleSignerInfoVerifierBuilder().build(certificate))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates a CMS signature for the given content using the provided private key and certificate.
     * @param content the content to be signed
     * @param privateKey the private key used for signing
     * @param certificate the X.509 certificate corresponding to the private key
     * @return the CMS signature as a byte array
     * @throws SignatureJCAException if an error occurs during the creation of the CMS signature
     */
    private byte[] createCMSSignature(InputStream content, PrivateKey privateKey, X509Certificate certificate) throws SignatureJCAException {
        try {
            CMSSignedDataGenerator generator = new CMSSignedDataGenerator();

            ContentSigner contentSigner = new JcaContentSignerBuilder(SIGNATURE_ALGORITHM).build(privateKey);

            generator.addSignerInfoGenerator(
                new JcaSignerInfoGeneratorBuilder(
                    new JcaDigestCalculatorProviderBuilder().build()
                ).build(contentSigner, certificate)
            );

            generator.addCertificates(new JcaCertStore(List.of(certificate)));

            CMSTypedData cmsData = new CMSProcessableByteArray(content.readAllBytes());

            return generator.generate(cmsData, false).getEncoded();
        } catch (IOException | CMSException | OperatorCreationException | CertificateException e) {
            throw new SignatureJCAException("Error creating CMS signature", e);
        }
    }

}
