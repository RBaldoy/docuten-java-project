package com.docuten.project.model;

import java.time.LocalDateTime;

import com.docuten.project.model.enums.SignatoryStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Entity class representing signatories
 * Signatories
 */
@Entity 
@Table (name = "signatory")
@Getter 
@Setter 
public class Signatory {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @Column (nullable = false)
    private String name;

    @Column (nullable = false)
    private String email;

    @Column (nullable = false)
    private int signOrder;

    @Enumerated(EnumType.STRING)
    @Column (nullable = false)
    private SignatoryStatus signStatus;

    @Column
    private LocalDateTime signDate;

    @Column (nullable = false)
    private LocalDateTime creationDate;

    @ManyToOne (optional = false)
    @JoinColumn (name = "signature_process_id", referencedColumnName = "id", nullable = false)
    private SignatureProcess signatureProcess;
}
