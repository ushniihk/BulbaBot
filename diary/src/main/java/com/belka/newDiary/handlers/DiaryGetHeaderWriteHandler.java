package com.belka.newDiary.handlers;

import com.belka.core.handlers.AbstractBelkaHandler;
import com.belka.core.models.BelkaEvent;
import com.belka.core.previous_step.dto.PreviousStepDto;
import com.belka.core.previous_step.service.PreviousService;
import com.belka.core.utils.CompletableFutureUtil;
import com.belka.stats.models.Stats;
import com.belka.stats.services.StatsService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import reactor.core.publisher.Flux;

import java.time.OffsetDateTime;

import static com.belka.newDiary.handlers.DiaryStartHandler.BUTTON_2;

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
    private final CompletableFutureUtil completableFutureUtil;

    @Override
    @Transactional
    public Flux<PartialBotApiMethod<?>> handle(BelkaEvent event) {
        return completableFutureUtil.supplyAsync(() -> {
            if (isMatchingCommand(event)) {
                return handleCommand(event);
            }
            return Flux.empty();
        }, CLASS_NAME).join();
    }

    private Flux<PartialBotApiMethod<?>> handleCommand(BelkaEvent event) {
        Long chatId = event.getChatId();
        savePreviousStep(chatId);
        saveStats(chatId);
        return Flux.just(sendMessage(chatId, HEADER));
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
        statsService.save(Stats.builder()
                .userId(userId)
                .handlerCode(CODE)
                .requestTime(OffsetDateTime.now())
                .build());
    }
}
