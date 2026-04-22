package com.nivora.pay.services.saga;

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
            log.info("Started saga instance with id : {}", sagaInstance.getId());

            return sagaInstance.getId();
        } catch (Exception e) {
            log.error("Error Starting saga", e);
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
            log.info("Saga Step not found for step name {}", stepName);
            throw new RuntimeException("Saga step not found");
        }
        SagaStep sagaStepDb = sagaStepRepository
                .findBySagaInstanceIdAndStatus(sagaInstanceId, SagaStepsStatus.PENDING)
                .stream()
                .filter(s -> s.getStepName().equals(stepName))
                .findFirst()
                .orElse(SagaStep.builder().sagaInstanceId(sagaInstanceId).stepName(stepName)
                        .status(SagaStepsStatus.PENDING)
                        .build());

        if (sagaStepDb.getId() == null) {
            sagaStepDb = sagaStepRepository.save(sagaStepDb);
        }

        try {
            SagaContext sagaContext = objectMapper.readValue(sagaInstance.getContext(), SagaContext.class);
            sagaStepDb.setStatus(SagaStepsStatus.RUNNING);
            sagaStepRepository.save(sagaStepDb);

           boolean success =  step.execute(sagaContext);

           if(success){
            sagaStepDb.setStatus(SagaStepsStatus.COMPLETED);
            sagaStepRepository.save(sagaStepDb);

            sagaInstance.setCurrentStep(stepName);
            sagaInstance.setStatus(SagaStatus.RUNNING);
            sagaInstanceRepository.save(sagaInstance);

            log.info("Step {} executed successfully", stepName);
            return true;
           }else{
            sagaStepDb.setStatus(SagaStepsStatus.FAILED);
            sagaStepRepository.save(sagaStepDb);
            log.error("Step {} failed",stepName);
            return false;
           }

        } catch (Exception e) {
            sagaStepDb.setStatus(SagaStepsStatus.FAILED);
            sagaStepRepository.save(sagaStepDb);
            log.error("Failed to Execute step {}" , stepName);
            return false;
        }

    }

    @Override
    public boolean compensateStep(Long sagaInstanceId, String stepName) {
        return false;
    }

    @Override
    public SagaInstance getSagaInstance(Long sagaInstanceId) {

        return null;
    }

    @Override
    public void compensateSaga(Long sagaInstanceId) {

    }

    @Override
    public void failSaga(Long sagaInstanceId) {

    }

    @Override
    public void completeSaga(Long sagaInstanceId) {

    }

}
