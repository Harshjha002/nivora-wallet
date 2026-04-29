package com.nivora.pay.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nivora.pay.entities.SagaInstance;


@Repository
public interface SagaInstanceRepository extends JpaRepository<SagaInstance , Long> {
      
    
    
}
