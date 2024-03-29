package com.belka.newDiary.handler;

import com.belka.core.handlers.AbstractBelkaHandler;
import com.belka.core.handlers.BelkaEvent;
import com.belka.core.previous_step.dto.PreviousStepDto;
import com.belka.core.previous_step.service.PreviousService;
import com.belka.stats.StatsDto;
import com.belka.stats.service.StatsService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import reactor.core.publisher.Flux;

import java.time.OffsetDateTime;
import java.util.concurrent.CompletableFuture;

import static com.belka.newDiary.handler.DiaryStartHandler.BUTTON_2;

@Component
@AllArgsConstructor
public class DiaryGetHeaderWriteHandler extends AbstractBelkaHandler {
    final static String CODE = "write diary header";
    private final static String NEXT_HANDLER = DiaryWriteHandler.CODE;
    private final static String PREVIOUS_HANDLER = DiaryStartHandler.CODE;
    private final static String HEADER = "write some words";
    private final static String PREVIOUS_DATA = DiaryStartHandler.CODE + BUTTON_2;
    private final PreviousService previousService;
    private final StatsService statsService;

    @Override
    @Transactional
    public Flux<PartialBotApiMethod<?>> handle(BelkaEvent event) {
        CompletableFuture<Flux<PartialBotApiMethod<?>>> future = CompletableFuture.supplyAsync(() -> {
            if (event.getUpdate().hasCallbackQuery() && event.getData().equals(PREVIOUS_DATA)) {
                Long chatId = event.getChatId();
                previousService.save(PreviousStepDto.builder()
                        .previousStep(CODE)
                        .nextStep(NEXT_HANDLER)
                        .userId(chatId)
                        .build());
                statsService.save(StatsDto.builder()
                        .userId(event.getChatId())
                        .handlerCode(CODE)
                        .requestTime(OffsetDateTime.now())
                        .build());
                return Flux.just(sendMessage(chatId, HEADER));
            }
            return Flux.empty();
        });
        return getCompleteFuture(future, event.getChatId());
    }
}
