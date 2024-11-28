package com.belka.audio.handlers;

import com.belka.audio.services.AudioService;
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
import java.util.concurrent.ExecutorService;

@Component
@AllArgsConstructor
@Slf4j
public class SaveAudioHandler extends AbstractBelkaHandler {
    final static String CODE = "save audio handler";
    final static String BUTTON_SHARE = "share";
    final static String BUTTON_PRIVATE = "let's keep it private";
    private final static String NEXT_HANDLER = ShareAudioHandler.CODE;
    private final static String PREVIOUS_HANDLER = RecordAudioHandler.CODE;
    private final static String CLASS_NAME = SaveAudioHandler.class.getSimpleName();
    private final static String HEADER = "do you want to share this";
    private final static String DELETE_MESSAGE = "message has been deleted";
    private final AudioService audioService;
    private final ExecutorService executorService;
    private final PreviousService previousService;
    private final StatsService statsService;
    private final CompletableFutureUtil completableFutureUtil;

    @Override
    @Transactional
    public Flux<PartialBotApiMethod<?>> handle(BelkaEvent event) {
        return completableFutureUtil.supplyAsync(() -> {
            if (isMatchingCommand(event, CODE)) {
                return handleMatchingCommand(event);
            }
            return Flux.empty();
        }, CLASS_NAME).join();
    }

    @Override
    protected boolean isMatchingCommand(BelkaEvent event, String code) {
        return event.getPrevious_step().equals(PREVIOUS_HANDLER) && event.getCode().equals(CODE) && event.isHasCallbackQuery();
    }

    private Flux<PartialBotApiMethod<?>> handleMatchingCommand(BelkaEvent event) {
        Long chatId = event.getChatId();
        if (event.getData().equals(RecordAudioHandler.BUTTON_SAVE)) {
            return handleSaveCommand(chatId);
        } else {
            return handleDeleteCommand(chatId);
        }
    }

    private Flux<PartialBotApiMethod<?>> handleSaveCommand(Long chatId) {
        PreviousStepDto previousStepDto = PreviousStepDto.builder()
                .previousStep(CODE)
                .nextStep(NEXT_HANDLER)
                .userId(chatId)
                // put the ID of the file to work with it at the stage where we will share the voice
                .data(previousService.getData(chatId))
                .build();
        savePreviousStep(previousStepDto, CLASS_NAME);
        recordStats(getStats(chatId));
        return Flux.just(getButtons(chatId));
    }

    private Flux<PartialBotApiMethod<?>> handleDeleteCommand(Long chatId) {
        audioService.deleteVoice(previousService.getData(chatId));
        savePreviousStep(getPreviousStep(chatId), CLASS_NAME);
        recordStats(getStats(chatId));
        return Flux.just(sendMessage(chatId, DELETE_MESSAGE));
    }


    private SendMessage getButtons(Long chatId) {
        SendMessage message = sendMessage(chatId, HEADER);

        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        InlineKeyboardButton shareButton = getButton(BUTTON_SHARE, BUTTON_SHARE);
        InlineKeyboardButton privateButton = getButton(BUTTON_PRIVATE, BUTTON_PRIVATE);

        rowInline.add(shareButton);
        rowInline.add(privateButton);
        rowsInLine.add(rowInline);
        markupInLine.setKeyboard(rowsInLine);

        message.setReplyMarkup(markupInLine);

        return message;
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
        executorService.execute(() -> {
                    statsService.save(stats);
                    log.info("Stats from {} have been recorded", CLASS_NAME);
                }
        );
    }
}
