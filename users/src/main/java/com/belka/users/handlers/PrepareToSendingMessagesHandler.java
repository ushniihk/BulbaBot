package com.belka.users.handlers;


import com.belka.core.handlers.AbstractBelkaHandler;
import com.belka.core.models.BelkaEvent;
import com.belka.core.previous_step.dto.PreviousStepDto;
import com.belka.core.utils.CompletableFutureUtil;
import com.belka.stats.models.Stats;
import com.belka.stats.services.StatsService;
import com.belka.users.configs.UserConfig;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import reactor.core.publisher.Flux;

import java.time.OffsetDateTime;
import java.util.concurrent.ExecutorService;

/**
 * the handler that starts the process of sending messages
 */
@Component
@AllArgsConstructor
@Slf4j
public class PrepareToSendingMessagesHandler extends AbstractBelkaHandler {

    final static String CODE = "/send";
    private final static String NEXT_HANDLER = SendingMessageHandler.CODE;
    private final static String CLASS_NAME = PrepareToSendingMessagesHandler.class.getSimpleName();
    private final static String HEADER = "write some text";
    private final ExecutorService executorService;
    private final UserConfig userConfig;
    private final StatsService statsService;
    private final CompletableFutureUtil completableFutureUtil;

    @Override
    @Transactional
    public Flux<PartialBotApiMethod<?>> handle(BelkaEvent event) {
        return completableFutureUtil.supplyAsync(() -> {
            if (isSubscribeCommand(event)) {
                return handleCommand(event);
            }
            return Flux.empty();
        }, CLASS_NAME).join();
    }

    private Flux<PartialBotApiMethod<?>> handleCommand(BelkaEvent event) {
        Long chatId = event.getChatId();
        savePreviousStep(getPreviousStep(chatId), CLASS_NAME);
        recordStats(getStats(chatId));
        return Flux.just(sendMessage(chatId, HEADER));
    }

    private boolean isSubscribeCommand(BelkaEvent event) {
        return event.isHasText()
                && event.getText().equalsIgnoreCase(CODE)
                && userConfig.getBotOwner().equals(event.getChatId());
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