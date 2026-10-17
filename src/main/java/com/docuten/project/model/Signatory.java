package com.docuten.project.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

    @Column (nullable = false)
    private String signStatus;

    @Column
    private Date signDate;

    @Column (nullable = false)
    private Date creationDate;

    @ManyToOne (optional = false)
    @JoinColumn (name = "signature_process_id", referencedColumnName = "id", nullable = false)
    private SignatureProcess signatureProcess;
}
