package com.belka.newDiary.handler;

import com.belka.core.handlers.AbstractBelkaHandler;
import com.belka.core.handlers.BelkaEvent;
import com.belka.core.previous_step.dto.PreviousStepDto;
import com.belka.core.previous_step.service.PreviousService;
import com.belka.newDiary.service.DiaryCalendarService;
import com.belka.stats.StatsDto;
import com.belka.stats.service.StatsService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.concurrent.CompletableFuture;

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

    @Override
    @Transactional
    public Flux<PartialBotApiMethod<?>> handle(BelkaEvent event) {
        CompletableFuture<Flux<PartialBotApiMethod<?>>> future = CompletableFuture.supplyAsync(() -> {
            try {
                if (isCalendarCall(event)) {
                    return handleCalendar(event);
                } else if (isChangingCalendarMonthCall(event)) {
                    return handleCalendarMonthChanging(event);
                }
            } catch (Exception e) {
                log.error("Error handling event in {}: {}", CLASS_NAME, e.getMessage(), e);
            }
            return Flux.empty();
        });
        return getCompleteFuture(future, event.getChatId());
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
        Long chatId = event.getChatId();
        LocalDate date = LocalDate.now();
        Integer year = date.getYear();
        Integer month = date.getMonth().getValue() - 1;

        savePreviousStep(chatId, NEXT_HANDLER);
        saveStats(event.getChatId());

        return Flux.just(diaryCalendarService.sendCalendarMessage(chatId, year, month));
    }

    private Flux<PartialBotApiMethod<?>> handleCalendarMonthChanging(BelkaEvent event) {
        String[] dateArray = event.getData().split("-");
        Integer year = Integer.parseInt(dateArray[1]);
        Integer month = Integer.parseInt(dateArray[2]);
        Long chatId = event.getChatId();

        SendMessage message = diaryCalendarService.sendCalendarMessage(chatId, year, month);
        message.setReplyToMessageId(event.getUpdate().getCallbackQuery().getMessage().getMessageId());

        savePreviousStep(chatId, null);
        saveStats(chatId);

        return Flux.just(editMessage(message, HEADER));
    }

    private void savePreviousStep(Long userId, String nextStep) {
        previousService.save(PreviousStepDto.builder()
                .previousStep(DiaryCalendarHandler.CODE)
                .userId(userId)
                .nextStep(nextStep)
                .build());
    }

    private void saveStats(Long userId) {
        statsService.save(StatsDto.builder()
                .userId(userId)
                .handlerCode(DiaryCalendarHandler.CODE)
                .requestTime(OffsetDateTime.now())
                .build());
    }
}