package com.docuten.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.docuten.project.model.SignatoryKey;

/**
 * Repository interface for managing SignatoryKey entities.
 * SignatoryKeyRepository
 */
@Repository 
public interface SignatoryKeyRepository extends JpaRepository<SignatoryKey, Long> {

}
