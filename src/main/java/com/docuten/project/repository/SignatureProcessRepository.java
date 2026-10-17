package com.docuten.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.docuten.project.model.SignatureProcess;

@Repository 
public interface SignatureProcessRepository extends JpaRepository<SignatureProcess, Long> {

}
