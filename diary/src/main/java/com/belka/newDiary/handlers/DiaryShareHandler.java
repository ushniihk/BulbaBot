package com.belka.newDiary.handlers;

import com.belka.core.handlers.AbstractBelkaHandler;
import com.belka.core.models.BelkaEvent;
import com.belka.core.previous_step.dto.PreviousStepDto;
import com.belka.core.previous_step.service.PreviousService;
import com.belka.core.utils.CompletableFutureUtil;
import com.belka.newDiary.services.DiaryService;
import com.belka.stats.models.Stats;
import com.belka.stats.services.StatsService;
import com.belka.users.services.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
@Slf4j
public class DiaryShareHandler extends AbstractBelkaHandler {
    final static String CODE = "SHARE_DIARY";
    private final static String NEXT_HANDLER = "";
    private final static String PREVIOUS_DATA_YES = DiaryWriteHandler.CODE + DiaryWriteHandler.YES_BUTTON;
    private final static String PREVIOUS_DATA_NO = DiaryWriteHandler.CODE + DiaryWriteHandler.NO_BUTTON;
    private final static String ANSWER_FOR_SHARING = "the note has been sent";
    private final static String ANSWER_FOR_SAVING = "the note has been saved";
    private final static String PREFIX_FOR_NOTE = "note from ";
    private final static String CLASS_NAME = DiaryShareHandler.class.getSimpleName();

    private final PreviousService previousService;
    private final DiaryService diaryService;
    private final StatsService statsService;
    private final UserService userService;
    private final CompletableFutureUtil completableFutureUtil;

    @Override
    @Transactional
    public Flux<PartialBotApiMethod<?>> handle(BelkaEvent event) {
        return completableFutureUtil.supplyAsync(() -> {
            if (isMatchingCommandYES(event)) {
                return handleYesCommand(event);
            } else if (isMatchingCommandNO(event)) {
                return handleNoCommand(event);
            }
            return Flux.empty();
        }, CLASS_NAME).join();
    }

    private Flux<PartialBotApiMethod<?>> handleYesCommand(BelkaEvent event) {
        Long chatId = event.getChatId();
        String note = PREFIX_FOR_NOTE + userService.getName(chatId) + "/n" + diaryService.getNote(LocalDate.now(), chatId);
        Collection<Long> followersId = userService.getFollowersId(chatId);
        Collection<SendMessage> messages = followersId.stream().map(id -> sendMessage(id, note)).collect(Collectors.toList());

        messages.add(sendMessage(chatId, ANSWER_FOR_SHARING));

        savePreviousAndStats(chatId);
        return Flux.fromIterable(messages);
    }

    private Flux<PartialBotApiMethod<?>> handleNoCommand(BelkaEvent event) {
        Long chatId = event.getChatId();
        savePreviousAndStats(chatId);
        return Flux.just(sendMessage(chatId, ANSWER_FOR_SAVING));
    }

    private boolean isMatchingCommandYES(BelkaEvent event) {
        return event.isHasCallbackQuery() && event.getData().equals(PREVIOUS_DATA_YES);
    }

    private boolean isMatchingCommandNO(BelkaEvent event) {
        return event.isHasCallbackQuery() && event.getData().equals(PREVIOUS_DATA_NO);
    }

    private void savePreviousAndStats(Long userId) {
        previousService.save(PreviousStepDto.builder()
                .previousStep(CODE)
                .nextStep(NEXT_HANDLER)
                .userId(userId)
                .build());
        statsService.save(Stats.builder()
                .userId(userId)
                .handlerCode(CODE)
                .requestTime(OffsetDateTime.now())
                .build());
    }
    //todo: make save stats multithreaded
}
