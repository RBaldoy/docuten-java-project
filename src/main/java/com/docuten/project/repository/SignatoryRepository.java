package com.docuten.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.docuten.project.model.Signatory;

/**
 * JPA Repository for managing Signatory entities
 */
@Repository
public interface SignatoryRepository extends JpaRepository<Signatory, Long> {

}
