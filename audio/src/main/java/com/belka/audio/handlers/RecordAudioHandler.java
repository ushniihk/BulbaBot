package com.belka.audio.handlers;

import com.belka.audio.services.AudioService;
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
import org.telegram.telegrambots.meta.api.objects.Voice;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@AllArgsConstructor
@Slf4j
public class RecordAudioHandler extends AbstractBelkaHandler {
    final static String CODE = "/record audio";
    private final static String NEXT_HANDLER = "save audio handler";
    private final static String CLASS_NAME = RecordAudioHandler.class.getSimpleName();
    final static String BUTTON_SAVE = "SAVE";
    final static String BUTTON_DELETE = "DELETE";
    private final static String HEADER_1 = "do we save this record?";
    private final AudioService audioService;
    private final StatsService statsService;
    private final CompletableFutureUtil completableFutureUtil;


    @Override
    @Transactional
    public Flux<PartialBotApiMethod<?>> handle(BelkaEvent event) {
        return completableFutureUtil.supplyAsync(() -> {
            if (isMatchingCommand(event, CODE)) {
                return handleVoiceMessage(event);
            }
            return Flux.empty();
        }, CLASS_NAME).join();
    }

    @Override
    protected boolean isMatchingCommand(BelkaEvent event, String code) {
        return event.isHasMessage() && event.getUpdate().getMessage().hasVoice();
    }

    private Flux<PartialBotApiMethod<?>> handleVoiceMessage(BelkaEvent event) {
        log.info("Start command handling in a class {}", CLASS_NAME);
        Long chatId = event.getChatId();
        Voice voice = event.getUpdate().getMessage().getVoice();
        audioService.saveVoice(voice, chatId);

        savePreviousStep(getPreviousStep(chatId, voice.getFileId()), CLASS_NAME);
        recordStats(getStats(chatId));
        return Flux.just(getButtons(event.getChatId()));
    }

    private SendMessage getButtons(Long chatId) {
        SendMessage message = sendMessage(chatId, HEADER_1);
        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        InlineKeyboardButton saveButton = getButton(BUTTON_SAVE, BUTTON_SAVE);
        InlineKeyboardButton deleteButton = getButton(BUTTON_DELETE, BUTTON_DELETE);

        rowInline.add(saveButton);
        rowInline.add(deleteButton);
        rowsInLine.add(rowInline);
        markupInLine.setKeyboard(rowsInLine);

        message.setReplyMarkup(markupInLine);

        return message;
    }

    private PreviousStepDto getPreviousStep(Long chatId, String data) {
        return PreviousStepDto.builder()
                .previousStep(CODE)
                .nextStep(NEXT_HANDLER)
                .userId(chatId)
                // put the ID of the file to work with it at the stage where we will share the voice
                .data(data)
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