package com.docuten.project.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Entity class representing a document
 * Document
 */
@Entity
@Table (name = "document")
@Getter 
@Setter
public class Document {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @Column (nullable = false)
    private String fileName;

    @Column (nullable = false)
    private String storagePath;

    @Column (nullable = false)
    private String hash;

    @Column (nullable = false)
    private int size;

    @Column (nullable = false)
    private LocalDateTime creationDate;

    @OneToOne(optional = false)
    @JoinColumn(name = "signature_process_id", referencedColumnName = "id", nullable = false, unique = true)
    private SignatureProcess signatureProcess;
}
