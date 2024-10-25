package com.belka.core.handlers;

import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import reactor.core.publisher.Flux;

/**
 * A handler that handle {@link BelkaEvent events} from user
 */
public interface BelkaHandler {
    /**
     * Handles a {@link BelkaEvent event} from a user.
     *
     * @param event the {@link BelkaEvent} to handle
     * @return collection of responses
     */
    Flux<PartialBotApiMethod<?>> handle(BelkaEvent event);
}
