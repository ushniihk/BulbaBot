package com.belka.newDiary.handlers;

import com.belka.core.handlers.AbstractBelkaHandler;
import com.belka.core.models.BelkaEvent;
import com.belka.core.previous_step.dto.PreviousStepDto;
import com.belka.core.previous_step.service.PreviousService;
import com.belka.core.utils.CompletableFutureUtil;
import com.belka.newDiary.services.DiaryService;
import com.belka.stats.models.Stats;
import com.belka.stats.services.StatsService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import reactor.core.publisher.Flux;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Component
@Slf4j
public class DiaryWriteHandler extends AbstractBelkaHandler {
    static final String CODE = "WRITE_DIARY";
    private final static String NEXT_HANDLER = DiaryShareHandler.CODE;
    private final static String PREVIOUS_HANDLER = DiaryGetHeaderWriteHandler.CODE;
    private final static String CLASS_NAME = DiaryWriteHandler.class.getSimpleName();
    final static String YES_BUTTON = "yes";
    final static String NO_BUTTON = "no";
    private final static String ANSWER = "got it";
    private final static String HEADER = "do you want to share this";
    private final PreviousService previousService;
    private final DiaryService diaryService;
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

    private boolean isMatchingCommand(BelkaEvent event) {
        return event.isHasText() && event.getPrevious_step().equals(PREVIOUS_HANDLER);
    }

    private Flux<PartialBotApiMethod<?>> handleCommand(BelkaEvent event) {
        Long chatId = event.getChatId();
        diaryService.addNote(chatId, event.getText());
        savePreviousAndStats(chatId);
        return Flux.just(sendMessage(chatId, ANSWER), getButtons(chatId));
    }

    private SendMessage getButtons(Long chatId) {
        SendMessage message = sendMessage(chatId, HEADER);

        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        InlineKeyboardButton getButton = getButton(YES_BUTTON, CODE + YES_BUTTON);
        InlineKeyboardButton writeButton = getButton(NO_BUTTON, CODE + NO_BUTTON);

        rowInline.add(getButton);
        rowInline.add(writeButton);
        rowsInLine.add(rowInline);
        markupInLine.setKeyboard(rowsInLine);

        message.setReplyMarkup(markupInLine);

        return message;
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
}
