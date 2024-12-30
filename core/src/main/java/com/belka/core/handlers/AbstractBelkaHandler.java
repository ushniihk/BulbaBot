package com.belka.core.handlers;

import com.belka.core.BelkaSendMessage;
import com.belka.core.models.BelkaEvent;
import com.belka.core.previous_step.dto.PreviousStepDto;
import com.belka.core.previous_step.service.PreviousService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Slf4j
@Component
public abstract class AbstractBelkaHandler implements BelkaHandler {
    private final static String TIMEOUT_MESSAGE = "sorry, it's tooooo long processing, try again or later";
    private final static String EXCEPTION_MESSAGE = "something was wrong and your request has been interrupted, try again or later";
    @Value("${bot.handler.timeout}")
    private Integer timeout;

    private BelkaSendMessage belkaSendMessage;
    private PreviousService previousService;

    @Autowired
    public void setBelkaSendMessage(BelkaSendMessage belkaSendMessage) {
        this.belkaSendMessage = belkaSendMessage;
    }

    @Autowired
    public void setPreviousService(PreviousService previousService) {
        this.previousService = previousService;
    }

    @Override
    abstract public Flux<PartialBotApiMethod<?>> handle(BelkaEvent event);

    protected SendMessage sendMessage(Long chatId, String answer) {
        return belkaSendMessage.sendMessage(chatId, answer);
    }

    protected PartialBotApiMethod<?> sendImageFromUrl(String url, Long chatId) {
        return belkaSendMessage.sendImageFromUrl(url, chatId);
    }

    protected PartialBotApiMethod<?> sendAudioFromLocalStorage(String url, Long chatId) {
        return belkaSendMessage.sendAudioFromLocalStorage(url, chatId);
    }

    protected PartialBotApiMethod<?> editMessage(SendMessage message, String text) {
        return belkaSendMessage.editMessage(message, text);
    }

    protected InlineKeyboardButton getButton(String text, String callbackData) {
        return InlineKeyboardButton.builder()
                .text(text)
                .callbackData(callbackData)
                .build();
    }

    protected List<InlineKeyboardButton> getRowInlineWithOneButton(String buttonText, String buttonCallBackData) {
        InlineKeyboardButton inlineKeyboardButton = getButton(buttonText, buttonCallBackData);
        List<InlineKeyboardButton> rowInlineOne = new ArrayList<>();
        rowInlineOne.add(inlineKeyboardButton);
        return rowInlineOne;
    }

    @Deprecated
    protected Flux<PartialBotApiMethod<?>> getCompleteFuture(CompletableFuture<Flux<PartialBotApiMethod<?>>> future, Long chatId) {
        return Mono.fromFuture(() -> future)  // Convert CompletableFuture<Flux> to Mono<Flux>
                .flatMapMany(Flux::from)  // Convert Mono<Flux> to Flux
                .timeout(Duration.ofSeconds(timeout))  // set timeout
                .switchIfEmpty(Flux.just(sendMessage(chatId, TIMEOUT_MESSAGE)))  // handle timeout
                .onErrorResume(e -> {  // handle exception
                    log.error("Request was interrupted", e);
                    return Flux.just(sendMessage(chatId, EXCEPTION_MESSAGE));
                });
    }

    protected void savePreviousStep(PreviousStepDto previousStep, String handlerName) {
        Mono.fromRunnable(() -> previousService.save(previousStep))
                .subscribeOn(Schedulers.boundedElastic()) // Для обработки блокирующего кода в другом потоке
                .doOnSuccess(unused -> log.info("Previous step from {} has been saved", handlerName))
                .doOnError(e -> log.error("Failed to save previous step from {}: {}", handlerName, e.getMessage()))
                .subscribe();
    }

    protected boolean isMatchingCommand(BelkaEvent event, String code) {
        return event.isHasText() && event.getText().equalsIgnoreCase(code) ||
                event.isHasCallbackQuery() && event.getData().equalsIgnoreCase(code);
    }
}

//todo add buttons to cache
