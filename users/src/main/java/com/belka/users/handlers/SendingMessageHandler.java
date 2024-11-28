package com.belka.users.handlers;

import com.belka.core.handlers.AbstractBelkaHandler;
import com.belka.core.models.BelkaEvent;
import com.belka.core.previous_step.dto.PreviousStepDto;
import com.belka.core.utils.CompletableFutureUtil;
import com.belka.stats.models.Stats;
import com.belka.stats.services.StatsService;
import com.belka.users.configs.UserConfig;
import com.belka.users.services.UserService;
import com.vdurmont.emoji.EmojiParser;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.concurrent.ExecutorService;

/**
 * the handler that processes the request to create a mailing list
 */
@Component
@AllArgsConstructor
@Slf4j
public class SendingMessageHandler extends AbstractBelkaHandler {

    final static String CODE = "SENDING MESSAGE";
    private final static String NEXT_HANDLER = "";
    private final static String PREVIOUS_HANDLER = PrepareToSendingMessagesHandler.CODE;
    private final static String CLASS_NAME = SendingMessageHandler.class.getSimpleName();
    private final ExecutorService executorService;
    private final UserService userService;
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
        String textToSend = EmojiParser.parseToUnicode(event.getText());
        savePreviousStep(getPreviousStep(chatId), CLASS_NAME);
        recordStats(getStats(chatId));
        return Flux.fromIterable(userService.getAll())
                .flatMap(userDto -> Mono.just(sendMessage(userDto.getId(), textToSend)));
    }

    private boolean isSubscribeCommand(BelkaEvent event) {
        return event.isHasText()
                && event.getPrevious_step().equals(PREVIOUS_HANDLER)
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
