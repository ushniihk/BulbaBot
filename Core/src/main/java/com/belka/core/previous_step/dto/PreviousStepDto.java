package com.belka.core.previous_step.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PreviousStepDto {
    private Long userId;
    private String previousStep;
}