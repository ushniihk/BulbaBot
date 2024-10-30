package com.belka.newDiary.handler;

import com.belka.core.handlers.AbstractBelkaHandler;
import com.belka.core.handlers.BelkaEvent;
import com.belka.core.previous_step.dto.PreviousStepDto;
import com.belka.core.previous_step.service.PreviousService;
import com.belka.stats.StatsDto;
import com.belka.stats.service.StatsService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import reactor.core.publisher.Flux;

import java.time.OffsetDateTime;
import java.util.concurrent.CompletableFuture;

import static com.belka.newDiary.handler.DiaryStartHandler.BUTTON_2;

@Component
@AllArgsConstructor
@Slf4j
public class DiaryGetHeaderWriteHandler extends AbstractBelkaHandler {
    final static String CODE = "write diary header";
    private final static String NEXT_HANDLER = DiaryWriteHandler.CODE;
    private final static String HEADER = "write some words";
    private final static String PREVIOUS_DATA = DiaryStartHandler.CODE + BUTTON_2;
    private final static String CLASS_NAME = DiaryGetHeaderWriteHandler.class.getSimpleName();

    private final PreviousService previousService;
    private final StatsService statsService;

    @Override
    @Transactional
    public Flux<PartialBotApiMethod<?>> handle(BelkaEvent event) {
        CompletableFuture<Flux<PartialBotApiMethod<?>>> future = CompletableFuture.supplyAsync(() -> {
            try {
                if (isMatchingCommand(event)) {
                    Long chatId = event.getChatId();
                    savePreviousStep(chatId);
                    saveStats(chatId);
                    return Flux.just(sendMessage(chatId, HEADER));
                }
            } catch (Exception e) {
                log.error("Error handling event in {}: {}", CLASS_NAME, e.getMessage(), e);
            }
            return Flux.empty();
        });
        return getCompleteFuture(future, event.getChatId());
    }

    private boolean isMatchingCommand(BelkaEvent event) {
        return event.isHasCallbackQuery() && event.getData().equals(PREVIOUS_DATA);
    }

    private void savePreviousStep(Long userId) {
        previousService.save(PreviousStepDto.builder()
                .previousStep(CODE)
                .nextStep(NEXT_HANDLER)
                .userId(userId)
                .build());
    }

    private void saveStats(Long userId) {
        statsService.save(StatsDto.builder()
                .userId(userId)
                .handlerCode(CODE)
                .requestTime(OffsetDateTime.now())
                .build());
    }
}
