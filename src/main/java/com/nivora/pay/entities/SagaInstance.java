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
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "saga_instance")
public class SagaInstance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SagaStatus status = SagaStatus.STARTED;

    @Column(name = "context", columnDefinition = "json")
    private String context;

    @Column(name = "current_step")
    private String currentStep;

    
    public void markAsStarted(){
       this.status =  SagaStatus.STARTED;
    }

    public void markAsFailed(){
        this.status = SagaStatus.FAILED;
    }

    public void markAsCompensated(){
        this.status = SagaStatus.COMPENSATED;
    }

    public void markAsRunning(){
        this.status = SagaStatus.RUNNING;
    }

    public void markAsCompensating(){
        this.status = SagaStatus.COMPENSATING;
    }

    public void markAsCompleted(){
        this.status = SagaStatus.COMPLETED;
    }
}