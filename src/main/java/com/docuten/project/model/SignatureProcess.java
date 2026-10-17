package com.docuten.project.model;

import java.sql.Date;

import com.docuten.project.model.enums.ProcessTypeEnum;
import com.docuten.project.model.enums.SignatureProcessStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

    @Column (nullable = false)
    private ProcessTypeEnum processType;

    @Column (nullable = false)
    private SignatureProcessStatus status;

    @Column (nullable = false)
    private Date creationDate;

    @Column (nullable = false)
    private Date expirationDate;

    private Date statusUpdateDate;

}
