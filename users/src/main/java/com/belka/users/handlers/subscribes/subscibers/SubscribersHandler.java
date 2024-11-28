package com.belka.users.handlers.subscribes.subscibers;

import com.belka.core.handlers.AbstractBelkaHandler;
import com.belka.core.models.BelkaEvent;
import com.belka.core.previous_step.dto.PreviousStepDto;
import com.belka.core.utils.CompletableFutureUtil;
import com.belka.stats.models.Stats;
import com.belka.stats.services.StatsService;
import com.belka.users.handlers.subscribes.subscriptions.unsubscribe.DeleteSubscriptionHandler;
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

/**
 * shows all commands that user can do with his subscribers
 */
@Component
@AllArgsConstructor
@Slf4j
public class SubscribersHandler extends AbstractBelkaHandler {
    public final static String CODE = "/Subscribers";
    private final static String NEXT_HANDLER = "";
    private final static String CLASS_NAME = SubscribersHandler.class.getSimpleName();
    private final static String HEADER = "that's your subscribers";
    private final static String BUTTON_SHOW_SUBSCRIBERS = "show all subscribers";
    private final static String BUTTON_DELETE_SUBSCRIBER = "to block someone"; //todo button for blocking subscriber
    private final StatsService statsService;
    private final ExecutorService executorService;
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
        savePreviousStep(getPreviousStep(chatId), CLASS_NAME);
        recordStats(getStats(chatId));
        return Flux.just(getButtons(event.getChatId()));
    }

    private SendMessage getButtons(Long chatId) {
        SendMessage message = sendMessage(chatId, HEADER);
        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();

        markupInLine.setKeyboard(getRowsInLine());
        message.setReplyMarkup(markupInLine);

        return message;
    }

    List<List<InlineKeyboardButton>> getRowsInLine() {
        List<InlineKeyboardButton> rowInlineShowSubscribers = getRowInlineWithOneButton(BUTTON_SHOW_SUBSCRIBERS, GetSubscribersHandler.CODE);
        List<InlineKeyboardButton> rowInlineDeleteSubscriber = getRowInlineWithOneButton(BUTTON_DELETE_SUBSCRIBER, DeleteSubscriptionHandler.CODE);

        return new ArrayList<>(List.of(
                rowInlineShowSubscribers,
                rowInlineDeleteSubscriber)
        );
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

//todo think how pass recordstats to handlers