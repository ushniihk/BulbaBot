package com.belka.BulbaBot.handler;

import com.belka.core.BelkaSendMessage;
import com.belka.core.converters.ConverterService;
import com.belka.core.handlers.BelkaHandler;
import com.belka.core.models.BelkaEvent;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import reactor.core.publisher.Flux;

import java.util.Collection;
import java.util.function.Function;

@Service
@AllArgsConstructor
public class HandlerServiceImpl implements HandlerService {
    private final static String RESPONSE = "sorry, but this command is not supported";
    private final Collection<BelkaHandler> handlers;
    private final ConverterService converterService;
    private final BelkaSendMessage belkaSendMessage;

    @Override
    public Flux<PartialBotApiMethod<?>> handle(Update update) {
        BelkaEvent event = converterService.convertTo(BelkaEvent.class, update);
        return Flux.fromStream(handlers.stream()
                        .map(handler -> handler.handle(event)))
                .filter(flux -> Boolean.TRUE.equals(flux.hasElements().block()))
                .flatMap(Function.identity())
                .switchIfEmpty(Flux.just(belkaSendMessage.sendMessage(event.getChatId(), RESPONSE)));
    }
}
