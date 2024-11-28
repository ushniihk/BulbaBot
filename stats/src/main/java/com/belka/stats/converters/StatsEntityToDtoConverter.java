package com.belka.stats.converters;

import com.belka.core.converters.BelkaConverter;
import com.belka.stats.models.Stats;
import com.belka.stats.entities.StatsEntity;
import org.springframework.stereotype.Component;

@Component
public class StatsEntityToDtoConverter implements BelkaConverter<StatsEntity, Stats> {
    @Override
    public Stats convert(StatsEntity value) {
        checkValue(value);
        return Stats.builder()
                .requestTime(value.getRequestTime())
                .handlerCode(value.getHandlerCode())
                .userId(value.getUserId())
                .build();
    }

    @Override
    public Class<Stats> getOutputType() {
        return Stats.class;
    }

    @Override
    public Class<StatsEntity> getInputType() {
        return StatsEntity.class;
    }
}
