

package com.nivora.pay.services.saga.steps;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.nivora.pay.services.saga.SagaStepInterface;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class SagaStepFactory {

    private final List<SagaStepInterface> steps;

    private Map<String, SagaStepInterface> stepMap;

    public static enum SagaStepType {
        DEBIT_SOURCE_WALLET_STEP,
        CREDIT_DESTINATION_WALLET_STEP,
        UPDATE_TRANSACTION_STATUS_STEP
    }

    public static final List<SagaStepType> TRANSFER_MONEY_SAGA_STEPS = List.of(
        SagaStepType.DEBIT_SOURCE_WALLET_STEP,
        SagaStepType.CREDIT_DESTINATION_WALLET_STEP,
        SagaStepType.UPDATE_TRANSACTION_STATUS_STEP
    );

    @PostConstruct
    public void init() {
        stepMap = steps.stream()
                .collect(Collectors.toMap(
                        SagaStepInterface::getStepName,
                        step -> step
                ));

        System.out.println("REGISTERED STEPS: " + stepMap.keySet());
    }

    public SagaStepInterface getSagaStep(String stepName) {
        log.info("REQUESTED STEP: {}" , stepName);
        return stepMap.get(stepName);
    }
}
