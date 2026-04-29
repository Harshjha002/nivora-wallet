package com.nivora.pay.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.nivora.pay.entities.SagaStep;
import com.nivora.pay.entities.SagaStepsStatus;


@Repository
public interface SagaStepRepository extends JpaRepository<SagaStep, Long> {
    
    List<SagaStep> findBySagaInstanceId(Long sagaInstanceId);

    Optional<SagaStep> findBySagaInstanceIdAndStepNameAndStatus(
            Long sagaInstanceId,
            String stepName,
            SagaStepsStatus status
    );

    
    @Query("SELECT s FROM SagaStep s WHERE s.sagaInstanceId = :sagaInstanceId AND s.status = SagaStepStatus.COMPLETED")
    List<SagaStep> findCompletedStepsBySagaInstanceId(@Param("sagaInstanceId") Long sagaInstanceId);

    List<SagaStep> findBySagaInstanceIdAndStatus(Long sagaInstanceId, SagaStepsStatus status);
}