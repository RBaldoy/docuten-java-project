package com.docuten.project.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

/**
 * Entity representing a signatory's keys (public key, private key and certificate).
 * SignatoryKey
 */
@Entity
@Table(name = "signatory_key")
@Getter
@Setter
public class SignatoryKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String publicKeyBase64;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String privateKeyBase64;
    
    @Column(columnDefinition = "TEXT")
    private String certificateBase64;
    
    @OneToOne (optional = false)
    @JoinColumn (name = "signatory_id", nullable = false, unique = true)
    private Signatory signatory;

}
