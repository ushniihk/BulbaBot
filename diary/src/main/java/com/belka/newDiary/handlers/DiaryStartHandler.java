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
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import reactor.core.publisher.Flux;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@AllArgsConstructor
@Slf4j
public class DiaryStartHandler extends AbstractBelkaHandler {
    final static String CODE = "/diary";
    private final static String NEXT_HANDLER = "";
    private final static String CLASS_NAME = DiaryStartHandler.class.getSimpleName();

    final static String BUTTON_1 = "get diary";
    final static String BUTTON_2 = "write diary";
    private final static String HEADER_1 = "what do you want?";
    private final PreviousService previousService;
    private final StatsService statsService;
    private final CompletableFutureUtil completableFutureUtil;

    @Override
    @Transactional
    public Flux<PartialBotApiMethod<?>> handle(BelkaEvent event) {
        return completableFutureUtil.supplyAsync(() -> {
            if (isMatchingCommand(event, CODE)) {
                return handleCommand(event);
            }
            return Flux.empty();
        }, CLASS_NAME).join();
    }

    private Flux<PartialBotApiMethod<?>> handleCommand(BelkaEvent event) {
        Long chatId = event.getChatId();
        savePreviousAndStats(chatId);
        return Flux.just(getButtons(chatId));
    }

    private SendMessage getButtons(Long chatId) {
        SendMessage message = sendMessage(chatId, HEADER_1);
        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        InlineKeyboardButton getButton = getButton(BUTTON_1, CODE + BUTTON_1);
        InlineKeyboardButton writeButton = getButton(BUTTON_2, CODE + BUTTON_2);

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
    //todo: make save stats multithreaded

}
