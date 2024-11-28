package com.belka.stats.converters;

import com.belka.core.converters.BelkaConverter;
import com.belka.stats.models.Stats;
import com.belka.stats.entities.StatsEntity;
import org.springframework.stereotype.Component;

@Component
public class StatsDtoToEntityConverter implements BelkaConverter<Stats, StatsEntity> {
    @Override
    public StatsEntity convert(Stats value) {
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
    public Class<Stats> getInputType() {
        return Stats.class;
    }
}
