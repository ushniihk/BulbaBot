package com.belka.users.handlers.subscribes;

import com.belka.core.handlers.AbstractBelkaHandler;
import com.belka.core.models.BelkaEvent;
import com.belka.core.previous_step.dto.PreviousStepDto;
import com.belka.core.utils.CompletableFutureUtil;
import com.belka.stats.models.Stats;
import com.belka.stats.services.StatsService;
import com.belka.users.handlers.subscribes.subscibers.SubscribersHandler;
import com.belka.users.handlers.subscribes.subscriptions.SubscriptionsHandler;
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
 * shows main subscribes opportunities
 */
@Component
@AllArgsConstructor
@Slf4j
public class SubscribesHandler extends AbstractBelkaHandler {
    public final static String CODE = "/Subscribes";
    private final static String NEXT_HANDLER = "";
    private final static String CLASS_NAME = SubscribesHandler.class.getSimpleName();
    private final static String HEADER = "and what do you want?";
    private final static String BUTTON_SUBSCRIPTIONS = "subscriptions";
    private final static String BUTTON_SUBSCRIBERS = "subscribers";
    private final ExecutorService executorService;
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
        savePreviousStep(getPreviousStep(chatId), CLASS_NAME);
        recordStats(getStats(chatId));
        return Flux.just(getButtons(event.getChatId()));
    }

    private SendMessage getButtons(Long chatId) {
        SendMessage message = sendMessage(chatId, HEADER);
        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();

        List<InlineKeyboardButton> rowInlineSubscriptions = getRowInlineWithOneButton(BUTTON_SUBSCRIPTIONS, SubscriptionsHandler.CODE);
        List<InlineKeyboardButton> rowInlineSubscribers = getRowInlineWithOneButton(BUTTON_SUBSCRIBERS, SubscribersHandler.CODE);

        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>(
                List.of(
                        rowInlineSubscriptions,
                        rowInlineSubscribers
                )
        );

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