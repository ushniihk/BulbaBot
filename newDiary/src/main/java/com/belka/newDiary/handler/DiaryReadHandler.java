package com.belka.newDiary.handler;

import com.belka.core.handlers.AbstractBelkaHandler;
import com.belka.core.handlers.BelkaEvent;
import com.belka.core.previous_step.dto.PreviousStepDto;
import com.belka.core.previous_step.service.PreviousService;
import com.belka.newDiary.service.DiaryCalendarService;
import com.belka.newDiary.service.DiaryService;
import com.belka.stats.StatsDto;
import com.belka.stats.service.StatsService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.concurrent.CompletableFuture;

@Component
@AllArgsConstructor
@Slf4j
public class DiaryReadHandler extends AbstractBelkaHandler {

    final static String CODE = "READ_DIARY";
    private final static String NEXT_HANDLER = DiaryCalendarHandler.CODE;
    private final static String PREVIOUS_HANDLER = DiaryStartHandler.CODE;
    private final static String CLASS_NAME = DiaryReadHandler.class.getSimpleName();
    private final DiaryService diaryService;
    private final PreviousService previousService;
    private final StatsService statsService;

    @Override
    @Transactional
    public Flux<PartialBotApiMethod<?>> handle(BelkaEvent event) {
        CompletableFuture<Flux<PartialBotApiMethod<?>>> future = CompletableFuture.supplyAsync(() -> {
            try {
                if (isMatchingCommand(event)) {
                    Long chatId = event.getChatId();
                    LocalDate date = parseDateFromEvent(event);

                    savePreviousStep(chatId);
                    saveStats(chatId);
                    return Flux.just(sendMessage(chatId, diaryService.getNote(date, chatId)));
                }
            } catch (Exception e) {
                log.error("Error handling event in {}: {}", CLASS_NAME, e.getMessage(), e);
            }
            return Flux.empty();
        });
        return getCompleteFuture(future, event.getChatId());
    }

    private LocalDate parseDateFromEvent(BelkaEvent event) {
        String[] dateArray = event.getData().split("\\.");
        int year = Integer.parseInt(dateArray[1]);
        int month = Integer.parseInt(dateArray[2]) + 1;
        int day = Integer.parseInt(dateArray[3]);
        return LocalDate.of(year, month, day);
    }

    private boolean isMatchingCommand(BelkaEvent event) {
        return event.isHasCallbackQuery() && event.getData().startsWith(DiaryCalendarService.DAY_DIARY_CALENDAR_CODE);
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
