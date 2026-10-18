package com.docuten.project.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import java.time.LocalDateTime;

@Entity
@Table (name = "document_version")
@Getter 
@Setter
@Builder 
public class DocumentVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column (nullable = false)
    private int versionNumber;

    @Column (nullable = false)
    private String storagePath;

    @Column (nullable = false)
    private String hash;

    @Column (nullable = false)
    private LocalDateTime creationDate;

    @OneToOne (optional = false)
    @JoinColumn (name = "document_id", nullable = false)
    private Document document;

}
