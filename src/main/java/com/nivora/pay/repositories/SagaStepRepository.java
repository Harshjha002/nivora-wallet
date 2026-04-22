package com.nivora.pay.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nivora.pay.entities.SagaStep;
import com.nivora.pay.entities.SagaStepsStatus;

public interface SagaStepRepository extends JpaRepository<SagaStep, Long> {
    
    List<SagaStep> findBySagaInstanceId(Long sagaInstanceId);

    @Query("SELECT s FROM SagaStep s WHERE s.sagaInstanceId = :sagaInstanceId AND s.status = 'COMPLETED'")
    List<SagaStep> findByCompletedStepsBySagaINstanceId(@Param("sagaInstanceId") Long sagaInstanceId);

    @Query("SELECT s FROM SagaStep s WHERE s.sagaInstanceId = :sagaInstanceId AND s.status IN ('COMPLETED', 'COMPENSATED')")
    List<SagaStep> findByCompletedOrCompensateedStepsBySagaINstanceId(@Param("sagaInstanceId") Long sagaInstanceId);

    List<SagaStep>  findBySagaInstanceIdAndStatus(Long sagaInstantId , SagaStepsStatus status);
}