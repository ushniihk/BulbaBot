package com.belka.stats.converter;

import com.belka.core.converters.BelkaConverter;
import com.belka.stats.StatsDto;
import com.belka.stats.StatsEntity;
import org.springframework.stereotype.Component;

@Component
public class StatsDtoToEntityConverter implements BelkaConverter<StatsDto, StatsEntity> {
    @Override
    public StatsEntity convert(StatsDto value) {
        checkValue(value);
        return StatsEntity.builder()
                .requestTime(value.getRequestTime())
                .handlerCode(value.getHandlerCode())
                .userId(value.getUserId())
                .build();
    }

    @Override
    public Class<StatsEntity> getOutputType() {
        return StatsEntity.class;
    }

    @Override
    public Class<StatsDto> getInputType() {
        return StatsDto.class;
    }
}
