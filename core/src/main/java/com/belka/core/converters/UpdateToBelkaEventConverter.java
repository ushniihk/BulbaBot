package com.belka.core.converters;

import com.belka.core.models.BelkaEvent;
import com.belka.core.previous_step.service.PreviousService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Optional;

@Component
public class UpdateToBelkaEventConverter implements BelkaConverter<Update, BelkaEvent> {

    private PreviousService previousService;

    @Autowired
    public void setPreviousService(@Lazy PreviousService previousService) {
        this.previousService = previousService;
    }

    @Override
    public BelkaEvent convert(Update value) {
        checkValue(value);
        Long chatId = getChatId(value);
        Integer updateId = value.getUpdateId();
        String previousStep = Optional.ofNullable(previousService.getPreviousStep(chatId)).orElse("");
        String nextStep = Optional.ofNullable(previousService.getNextStep(chatId)).orElse("");
        boolean hasMessage = value.hasMessage();
        boolean hasText = hasMessage && value.getMessage().hasText();
        boolean hasCallbackQuery = value.hasCallbackQuery();
        String text = value.hasMessage() && value.getMessage().hasText() ? value.getMessage().getText() : null;
        String data = value.hasCallbackQuery() ? value.getCallbackQuery().getData() : null;

        return BelkaEvent.builder()
                .update(value)
                .updateId(updateId)
                .data(data)
                .chatId(chatId)
                .text(text)
                .previous_step(previousStep)
                .code(nextStep)
                .hasMessage(hasMessage)
                .hasCallbackQuery(hasCallbackQuery)
                .hasText(hasText)
                .build();
    }

    @Override
    public Class<BelkaEvent> getOutputType() {
        return BelkaEvent.class;
    }

    @Override
    public Class<Update> getInputType() {
        return Update.class;
    }

    public Long getChatId(Update update) {
        if (update.hasMessage()) {
            return update.getMessage().getChatId();
        }
        if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getMessage().getChatId();
        }
        throw new IllegalArgumentException("Received an Update without a chat ID");
    }
}
