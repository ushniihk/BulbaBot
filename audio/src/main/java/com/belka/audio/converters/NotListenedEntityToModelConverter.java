package com.belka.audio.converters;

import com.belka.audio.entities.NotListenedEntity;
import com.belka.audio.models.NotListened;
import com.belka.core.converters.BelkaConverter;
import org.springframework.stereotype.Component;

@Component
public class NotListenedEntityToModelConverter implements BelkaConverter<NotListenedEntity, NotListened> {
    @Override
    public NotListened convert(NotListenedEntity value) {
        checkValue(value);
        return NotListened.builder()
                .audioId(value.getAudioId())
                .subscriber(value.getSubscriber())
                .build();
    }

    @Override
    public Class<NotListened> getOutputType() {
        return NotListened.class;
    }

    @Override
    public Class<NotListenedEntity> getInputType() {
        return NotListenedEntity.class;
    }
}
