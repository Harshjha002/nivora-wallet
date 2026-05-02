package com.nivora.pay.services;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.nivora.pay.services.saga.SagaOrchestrator;
import com.nivora.pay.services.saga.steps.SagaStepFactory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransferSagaAsyncService {

    private final SagaOrchestrator sagaOrchestrator;

    @Async
    public void executeTransferSaga(Long sagaInstanceId) {

        log.info("Executing transfer saga {}", sagaInstanceId);

        try {
            for (SagaStepFactory.SagaStepType step : SagaStepFactory.TRANSFER_MONEY_SAGA_STEPS) {

                boolean success = sagaOrchestrator.executeStep(
                        sagaInstanceId,
                        step.toString()
                );

                if (!success) {
                    log.error("Step {} failed", step);
                    sagaOrchestrator.failSaga(sagaInstanceId);
                    return;
                }
            }

            sagaOrchestrator.completeSaga(sagaInstanceId);
            log.info("Saga {} completed", sagaInstanceId);

        } catch (Exception e) {
            log.error("Saga {} failed", sagaInstanceId, e);
            sagaOrchestrator.failSaga(sagaInstanceId);
        }
    }
}
