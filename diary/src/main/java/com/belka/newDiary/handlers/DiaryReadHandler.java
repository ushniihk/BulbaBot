package com.belka.newDiary.handlers;

import com.belka.core.handlers.AbstractBelkaHandler;
import com.belka.core.models.BelkaEvent;
import com.belka.core.previous_step.dto.PreviousStepDto;
import com.belka.core.previous_step.service.PreviousService;
import com.belka.core.utils.CompletableFutureUtil;
import com.belka.newDiary.services.DiaryCalendarService;
import com.belka.newDiary.services.DiaryService;
import com.belka.stats.models.Stats;
import com.belka.stats.services.StatsService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Component
@AllArgsConstructor
@Slf4j
public class DiaryReadHandler extends AbstractBelkaHandler {

    final static String CODE = "READ_DIARY";
    private final static String NEXT_HANDLER = DiaryCalendarHandler.CODE;
    private final static String CLASS_NAME = DiaryReadHandler.class.getSimpleName();
    private final DiaryService diaryService;
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
        log.info("Start command handling in a class {}", CLASS_NAME);
        Long chatId = event.getChatId();
        LocalDate date = parseDateFromEvent(event);

        savePreviousStep(chatId);
        recordStats(getStats(chatId));
        return Flux.just(sendMessage(chatId, diaryService.getNote(date, chatId)));
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

    private Stats getStats(Long chatId) {
        return Stats.builder()
                .userId(chatId)
                .handlerCode(CODE)
                .requestTime(OffsetDateTime.now())
                .build();
    }

    private void recordStats(Stats stats) {
        Mono.fromRunnable(() -> statsService.save(stats))
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(unused -> log.info("Stats from {} have been recorded", CLASS_NAME))
                .doOnError(e -> log.error("Failed to record stats in {}: {}", CLASS_NAME, e.getMessage()))
                .subscribe();
    }
}
