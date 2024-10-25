package com.belka.core.previous_step.dto;

import com.belka.core.previous_step.interfaces.PreviousStep;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PreviousStepDto implements PreviousStep {
    private Long userId;
    private String previousStep;
    private String nextStep;
    private String data;
}
