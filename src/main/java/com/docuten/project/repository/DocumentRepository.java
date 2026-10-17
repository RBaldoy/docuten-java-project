package com.docuten.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.docuten.project.model.Document;

/**
 * JPA Repository for managing Document entities
 */
@Repository 
public interface DocumentRepository extends JpaRepository<Document, Long> {

}
