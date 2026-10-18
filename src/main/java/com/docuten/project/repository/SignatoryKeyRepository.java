package com.docuten.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.docuten.project.model.SignatoryKey;

/**
 * Repository interface for managing SignatoryKey entities.
 * SignatoryKeyRepository
 */
public interface SignatoryKeyRepository extends JpaRepository<SignatoryKey, Long> {

}
