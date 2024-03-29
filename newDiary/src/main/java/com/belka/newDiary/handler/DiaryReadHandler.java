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
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.concurrent.CompletableFuture;

@Component
@AllArgsConstructor
public class DiaryReadHandler extends AbstractBelkaHandler {

    final static String CODE = "READ_DIARY";
    private final static String NEXT_HANDLER = DiaryCalendarHandler.CODE;
    private final static String PREVIOUS_HANDLER = DiaryStartHandler.CODE;
    private final DiaryService diaryService;
    private final PreviousService previousService;
    private final StatsService statsService;

    @Override
    @Transactional
    public Flux<PartialBotApiMethod<?>> handle(BelkaEvent event) {
        CompletableFuture<Flux<PartialBotApiMethod<?>>> future = CompletableFuture.supplyAsync(() -> {
            if (event.isHasCallbackQuery() && event.getData().startsWith(DiaryCalendarService.DAY_DIARY_CALENDAR_CODE)) {
                Long chatId = event.getChatId();
                String[] dateArray = event.getData().split("\\.");
                int YEAR = Integer.parseInt(dateArray[1]);
                int MONTH = Integer.parseInt(dateArray[2]) + 1;
                int DAY = Integer.parseInt(dateArray[3]);
                LocalDate date = LocalDate.of(YEAR, MONTH, DAY);

                previousService.save(PreviousStepDto.builder()
                        .previousStep(CODE)
                        .nextStep(NEXT_HANDLER)
                        .userId(chatId)
                        .build());
                statsService.save(StatsDto.builder()
                        .userId(chatId)
                        .handlerCode(CODE)
                        .requestTime(OffsetDateTime.now())
                        .build());
                return Flux.just(sendMessage(chatId, diaryService.getNote(date, chatId)));
            }
            return Flux.empty();
        });
        return getCompleteFuture(future, event.getChatId());
    }
}
