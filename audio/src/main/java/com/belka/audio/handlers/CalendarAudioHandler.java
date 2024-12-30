package com.belka.audio.handlers;

import com.belka.audio.services.AudioCalendarService;
import com.belka.core.handlers.AbstractBelkaHandler;
import com.belka.core.models.BelkaEvent;
import com.belka.core.previous_step.dto.PreviousStepDto;
import com.belka.core.utils.CompletableFutureUtil;
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
public class CalendarAudioHandler extends AbstractBelkaHandler {
    final static String CODE = "/audio calendar";
    private final static String NEXT_HANDLER = "";
    private final static String CLASS_NAME = CalendarAudioHandler.class.getSimpleName();
    private final static String HEADER = "Calendar";
    private final AudioCalendarService calendarService;
    private final StatsService statsService;
    private final CompletableFutureUtil completableFutureUtil;

    @Override
    @Transactional
    public Flux<PartialBotApiMethod<?>> handle(BelkaEvent event) {
        return completableFutureUtil.supplyAsync(() -> {
            if (isMatchingCommand(event, CODE)) {
                return handleCalendarCallback(event);
            } else if (isMonthChangeCallback(event)) {
                return handleMonthChangeCallback(event);
            }
            return Flux.empty();
        }, CLASS_NAME).join();
    }

    @Override
    protected boolean isMatchingCommand(BelkaEvent event, String code) {
        return event.isHasCallbackQuery() && event.getData().equals(CODE);
    }

    private boolean isMonthChangeCallback(BelkaEvent event) {
        return event.getUpdate().hasCallbackQuery() &&
                (event.getData().startsWith(AudioCalendarService.AUDIO_CALENDAR_NEXT_MONTH) ||
                        event.getData().startsWith(AudioCalendarService.AUDIO_CALENDAR_PREV_MONTH));
    }


    private Flux<PartialBotApiMethod<?>> handleCalendarCallback(BelkaEvent event) {
        log.info("Start command handling in a class {}", CLASS_NAME);
        Long chatId = event.getChatId();
        LocalDate date = LocalDate.now();
        Integer year = date.getYear();
        Integer month = date.getMonth().getValue() - 1;
        savePreviousStep(getPreviousStep(chatId), CLASS_NAME);
        recordStats(getStats(chatId));
        SendMessage message = calendarService.sendCalendarMessage(chatId, year, month);
        message.setReplyToMessageId(event.getUpdate().getCallbackQuery().getMessage().getMessageId());
        return Flux.just(editMessage(message, HEADER));
    }

    private Flux<PartialBotApiMethod<?>> handleMonthChangeCallback(BelkaEvent event) {
        Long chatId = event.getChatId();
        String[] dateArray = event.getData().split("-");
        Integer YEAR = Integer.parseInt(dateArray[1]);
        Integer MONTH = Integer.parseInt(dateArray[2]);
        SendMessage message = calendarService.sendCalendarMessage(chatId, YEAR, MONTH);
        message.setReplyToMessageId(event.getUpdate().getCallbackQuery().getMessage().getMessageId());
        savePreviousStep(getPreviousStep(chatId), CLASS_NAME);
        recordStats(getStats(chatId));
        return Flux.just(editMessage(message, HEADER));
    }

    private PreviousStepDto getPreviousStep(Long chatId) {
        return PreviousStepDto.builder()
                .previousStep(CODE)
                .nextStep(NEXT_HANDLER)
                .userId(chatId)
                .data("")
                .build();
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
