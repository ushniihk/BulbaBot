package com.belka.newDiary.handlers;

import com.belka.core.handlers.AbstractBelkaHandler;
import com.belka.core.models.BelkaEvent;
import com.belka.core.previous_step.dto.PreviousStepDto;
import com.belka.core.previous_step.service.PreviousService;
import com.belka.core.utils.CompletableFutureUtil;
import com.belka.newDiary.services.DiaryCalendarService;
import com.belka.stats.models.Stats;
import com.belka.stats.services.StatsService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Component
@AllArgsConstructor
@Slf4j
public class DiaryCalendarHandler extends AbstractBelkaHandler {
    final static String CODE = "CALENDAR_DIARY";
    private final static String NEXT_HANDLER = "";
    private final static String HEADER = "Calendar";
    private final static String PREVIOUS_DATA = DiaryStartHandler.CODE + DiaryStartHandler.BUTTON_1;
    private final static String CLASS_NAME = DiaryCalendarHandler.class.getSimpleName();

    private final PreviousService previousService;
    private final DiaryCalendarService diaryCalendarService;
    private final StatsService statsService;
    private final CompletableFutureUtil completableFutureUtil;

    @Override
    @Transactional
    public Flux<PartialBotApiMethod<?>> handle(BelkaEvent event) {
        return completableFutureUtil.supplyAsync(() -> {
            if (isCalendarCall(event)) {
                return handleCalendar(event);
            } else if (isChangingCalendarMonthCall(event)) {
                return handleCalendarMonthChanging(event);
            }
            return Flux.empty();
        }, CLASS_NAME).join();
    }

    protected boolean isCalendarCall(BelkaEvent event) {
        return event.isHasCallbackQuery() && event.getData().equals(PREVIOUS_DATA);
    }

    protected boolean isChangingCalendarMonthCall(BelkaEvent event) {
        return event.getUpdate().hasCallbackQuery() &&
                (event.getData().startsWith(DiaryCalendarService.DIARY_CALENDAR_PREV_MONTH) ||
                        event.getData().startsWith(DiaryCalendarService.DIARY_CALENDAR_NEXT_MONTH));
    }

    private Flux<PartialBotApiMethod<?>> handleCalendar(BelkaEvent event) {
        log.info("Start command handling in a class {}", CLASS_NAME);
        Long chatId = event.getChatId();
        LocalDate date = LocalDate.now();
        Integer year = date.getYear();
        Integer month = date.getMonth().getValue() - 1;

        savePreviousStep(chatId, NEXT_HANDLER);
        recordStats(getStats(chatId));

        return Flux.just(diaryCalendarService.sendCalendarMessage(chatId, year, month));
    }

    private Flux<PartialBotApiMethod<?>> handleCalendarMonthChanging(BelkaEvent event) {
        log.info("Start command handling in a class {}", CLASS_NAME);
        String[] dateArray = event.getData().split("-");
        Integer year = Integer.parseInt(dateArray[1]);
        Integer month = Integer.parseInt(dateArray[2]);
        Long chatId = event.getChatId();

        SendMessage message = diaryCalendarService.sendCalendarMessage(chatId, year, month);
        message.setReplyToMessageId(event.getUpdate().getCallbackQuery().getMessage().getMessageId());

        savePreviousStep(chatId, null);
        recordStats(getStats(chatId));

        return Flux.just(editMessage(message, HEADER));
    }

    private void savePreviousStep(Long userId, String nextStep) {
        previousService.save(PreviousStepDto.builder()
                .previousStep(DiaryCalendarHandler.CODE)
                .userId(userId)
                .nextStep(nextStep)
                .build());
    }

    private Stats getStats(Long chatId) {
        return Stats.builder()
                .userId(chatId)
                .handlerCode(DiaryCalendarHandler.CODE)
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