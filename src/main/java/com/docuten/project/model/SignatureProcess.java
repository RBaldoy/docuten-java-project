package com.docuten.project.model;

import java.time.LocalDateTime;
import java.util.List;

import com.docuten.project.model.enums.ProcessTypeEnum;
import com.docuten.project.model.enums.SignatureProcessStatusEnum;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Entity class representing a signature process
 * SignatureProcess
 */
@Entity
@Table(name = "signature_process")
@Getter 
@Setter
public class SignatureProcess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column (nullable = false)
    private ProcessTypeEnum processType;

    @Enumerated(EnumType.STRING)
    @Column (nullable = false)
    private SignatureProcessStatusEnum status;

    @Column (nullable = false)
    private LocalDateTime creationDate;

    @Column (nullable = false)
    private LocalDateTime expirationDate;

    @Column
    private LocalDateTime statusUpdateDate;

    @OneToOne (mappedBy = "signatureProcess", cascade = CascadeType.ALL)
    private Document document;

    @OneToMany (mappedBy = "signatureProcess", cascade = CascadeType.ALL)
    @OrderBy ("signOrder ASC")
    private List<Signatory> signatories;
}
