package com.docuten.project.service.impl;

import com.docuten.project.service.ISignatureJCAService;
import com.docuten.project.exception.SignatureJCAException;
import jakarta.transaction.Transactional;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.List;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;

import org.apache.pdfbox.Loader;
import org.springframework.stereotype.Service;

@Service
public class SignatureJCAService implements ISignatureJCAService {

    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";

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
