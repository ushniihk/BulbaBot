package com.belka.core.models;

import lombok.Builder;
import lombok.Data;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * Event from telegram API
 */
@Data
@Builder
public class BelkaEvent {
    /**
     * Telegram update object
     */
    private final Update update;
    /**
     * User's id
     */
    private final Long chatId;
    /**
     * {@link Update update's } id
     */
    private final Integer updateId;
    /**
     * Previous handler's CODE
     */
    private final String previous_step;
    /**
     * Current handler's CODE
     */
    private final String code;
    /**
     * Callback data
     */
    private final String data;
    /**
     * Message text
     */
    private final String text;
    /**
     * Flag indicating if the update has a message
     */
    private final boolean hasMessage;
    /**
     * Flag indicating if the message has text
     */
    private final boolean hasText;
    /**
     * Flag indicating if the update has a callback query
     */
    private final boolean hasCallbackQuery;
}
