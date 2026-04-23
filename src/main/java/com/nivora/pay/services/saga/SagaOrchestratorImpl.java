package com.nivora.pay.services.saga;

import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivora.pay.entities.SagaInstance;
import com.nivora.pay.entities.SagaStatus;
import com.nivora.pay.entities.SagaStep;
import com.nivora.pay.entities.SagaStepsStatus;
import com.nivora.pay.repositories.SagaInstanceRepository;
import com.nivora.pay.repositories.SagaStepRepository;
import com.nivora.pay.services.saga.steps.SagaStepFactory;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SagaOrchestratorImpl implements SagaOrchestrator {

    private final ObjectMapper objectMapper;
    private final SagaInstanceRepository sagaInstanceRepository;
    private final SagaStepFactory sagaStepFactory;
    private final SagaStepRepository sagaStepRepository;

    @Override
    public Long startSaga(SagaContext context) {

        try {
            String contextJson = objectMapper.writeValueAsString(context);
            SagaInstance sagaInstance = SagaInstance.builder()
                    .context(contextJson)
                    .status(SagaStatus.STARTED)
                    .build();

            sagaInstance = sagaInstanceRepository.save(sagaInstance);
            log.info("Started saga instance with id: {}", sagaInstance.getId());

            return sagaInstance.getId();
        } catch (Exception e) {
            log.error("Error starting saga", e);
            throw new RuntimeException("Error Starting saga", e);
        }

    }

    @Override
    @Transactional
    public boolean executeStep(Long sagaInstanceId, String stepName) {

        SagaInstance sagaInstance = sagaInstanceRepository.findById(sagaInstanceId)
                .orElseThrow(() -> new RuntimeException("Saga instance not found"));

        SagaStepInterface step = sagaStepFactory.getSagaStep(stepName);

        if (step == null) {
            log.info("Saga step not found for step name {}", stepName);
            throw new RuntimeException("Saga step not found");
        }

        SagaStep sagaStepDb = sagaStepRepository
                .findBySagaInstanceIdAndStepNameAndStatus(sagaInstanceId, stepName, SagaStepsStatus.PENDING)
                .orElse(SagaStep.builder().sagaInstanceId(sagaInstanceId).stepName(stepName)
                        .status(SagaStepsStatus.PENDING)
                        .build());

        if (sagaStepDb.getId() == null) {
            sagaStepDb = sagaStepRepository.save(sagaStepDb);
        }

        try {
            SagaContext sagaContext = objectMapper.readValue(sagaInstance.getContext(), SagaContext.class);
            sagaStepDb.markAsRunning();
            sagaStepRepository.save(sagaStepDb);

            boolean success = step.execute(sagaContext);

            if (success) {
                sagaStepDb.markAsCompleted();
                sagaStepRepository.save(sagaStepDb);

                sagaInstance.setCurrentStep(stepName);
                sagaInstance.markAsRunning();
                sagaInstanceRepository.save(sagaInstance);

                log.info("Step {} executed successfully", stepName);
                return true;
            } else {
                sagaStepDb.markAsFailed();
                sagaStepRepository.save(sagaStepDb);
                log.error("Step {} failed", stepName);
                return false;
            }

        } catch (Exception e) {
            sagaStepDb.markAsFailed();
            sagaStepRepository.save(sagaStepDb);
            log.error("Failed to execute step {}", stepName);
            return false;
        }

    }

    @Override
    public boolean compensateStep(Long sagaInstanceId, String stepName) {
        SagaInstance sagaInstance = sagaInstanceRepository.findById(sagaInstanceId)
                .orElseThrow(() -> new RuntimeException("Saga instance not found"));

        SagaStepInterface step = sagaStepFactory.getSagaStep(stepName);

        if (step == null) {
            log.info("Saga step not found for step name {}", stepName);
            throw new RuntimeException("Saga step not found");
        }

        SagaStep sagaStepDb = sagaStepRepository
                .findBySagaInstanceIdAndStepNameAndStatus(sagaInstanceId, stepName, SagaStepsStatus.COMPLETED)
                .orElse(null);

        if (sagaStepDb == null) {
    log.info("Step {} not found in DB for saga instance {}", stepName, sagaInstanceId);
    return true;
}
        try {
            SagaContext sagaContext = objectMapper.readValue(sagaInstance.getContext(), SagaContext.class);
            sagaStepDb.markAsCompensating();
            sagaStepRepository.save(sagaStepDb);

            boolean success = step.compensate(sagaContext);

            if (success) {
                sagaStepDb.markAsCompensated();
                sagaStepRepository.save(sagaStepDb);

                log.info("Step {} compensated successfully", stepName);
                return true;
            } else {
                sagaStepDb.markAsFailed();
                sagaStepRepository.save(sagaStepDb);
                log.error("Step {} failed", stepName);
                return false;
            }

        } catch (Exception e) {
            sagaStepDb.markAsFailed();
            sagaStepRepository.save(sagaStepDb);
            log.error("Failed to execute step {}", stepName);
            return false;
        }

    }

    @Override
    public void compensateSaga(Long sagaInstanceId) {
        SagaInstance sagaInstance = sagaInstanceRepository.findById(sagaInstanceId)
                .orElseThrow(() -> new RuntimeException("Saga instance not found"));
        // marking saga status as compensation in DB
        sagaInstance.markAsCompensating();
        sagaInstanceRepository.save(sagaInstance);

        // find all steps that are completed
        List<SagaStep> completedSteps = sagaStepRepository.findCompletedStepsBySagaInstanceId(sagaInstanceId);

        boolean allCompensated = true;

        for (SagaStep completedStep : completedSteps) {
            boolean compensated = this.compensateStep(sagaInstanceId, completedStep.getStepName());
            if (!compensated) {
                allCompensated = false;
            }
        }

        if (allCompensated) {
            sagaInstance.markAsCompensated();
            sagaInstanceRepository.save(sagaInstance);
            log.info("Saga {} compensated successfully", sagaInstanceId);
        } else {
            log.error("Saga {} compensation failed", sagaInstanceId);
        }

    }

    @Override
    public SagaInstance getSagaInstance(Long sagaInstanceId) {

        return sagaInstanceRepository.findById(sagaInstanceId)
                .orElseThrow(() -> new RuntimeException("Saga instance not found"));
    }

    @Override
    public void failSaga(Long sagaInstanceId) {
        SagaInstance sagaInstance = sagaInstanceRepository.findById(sagaInstanceId)
                .orElseThrow(() -> new RuntimeException("Saga instance not found"));
        sagaInstance.markAsFailed();
        sagaInstanceRepository.save(sagaInstance);
    }

    @Override
    public void completeSaga(Long sagaInstanceId) {
        SagaInstance sagaInstance = sagaInstanceRepository.findById(sagaInstanceId)
                .orElseThrow(() -> new RuntimeException("Saga instance not found"));
        sagaInstance.markAsCompleted();
        sagaInstanceRepository.save(sagaInstance);
    }

}
