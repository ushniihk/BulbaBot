package com.belka.core.converters;

import com.belka.core.previous_step.dto.PreviousStepDto;
import com.belka.core.previous_step.entity.PreviousStepEntity;
import org.springframework.stereotype.Component;

@Component
public class PreviousStepDTOToEntityConverter implements BelkaConverter<PreviousStepDto, PreviousStepEntity> {
    @Override
    public PreviousStepEntity convert(PreviousStepDto value) {
        checkValue(value);
        return PreviousStepEntity.builder()
                .previousStep(value.getPreviousStep())
                .userId(value.getUserId())
                .nextStep(value.getNextStep())
                .data(value.getData())
                .build();
    }

    @Override
    public Class<PreviousStepEntity> getOutputType() {
        return PreviousStepEntity.class;
    }

    @Override
    public Class<PreviousStepDto> getInputType() {
        return PreviousStepDto.class;
    }
}
