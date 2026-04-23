package com.nivora.pay.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "saga_step")
public class SagaStep {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "saga_instance_id", nullable = false)
    private Long sagaInstanceId;

    @Column(name = "step_name", nullable = false)
    private String stepName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SagaStepsStatus status;

    @Column(name = "error_message", nullable = true)
    private String errorMessage;

    @Column(name = "step_data", columnDefinition = "json")
    private String stepData;

    public void markAsCompensated(){
       this.status =  SagaStepsStatus.COMPENSATED;
    }

    public void markAsFailed(){
        this.status = SagaStepsStatus.FAILED;
    }

    public void markAsPending(){
        this.status = SagaStepsStatus.PENDING;
    }

    public void markAsRunning(){
        this.status = SagaStepsStatus.RUNNING;
    }

    public void markAsCompensating(){
        this.status = SagaStepsStatus.COMPENSATING;
    }

    public void markAsCompleted(){
        this.status = SagaStepsStatus.COMPLETED;
    }

}
