package com.belka.BulbaBot.handler;

import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import reactor.core.publisher.Flux;

/**
 * A service that aggregates the work of all {@link com.belka.core.handlers.BelkaHandler handlers}.
 * Each handler processes the incoming updates and generates responses.
 */
public interface HandlerService {
    /**
     * Distributes the incoming update to the appropriate handlers for processing.
     * Each handler may generate one or more responses, which are returned as a Flux.
     *
     * @param update The incoming update from Telegram
     * @return A Flux of responses to be sent back to the user
     */
    Flux<PartialBotApiMethod<?>> handle(Update update);
}
