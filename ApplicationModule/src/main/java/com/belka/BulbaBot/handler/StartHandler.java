package com.belka.BulbaBot.handler;

import com.belka.core.handlers.AbstractBelkaHandler;
import com.belka.core.models.BelkaEvent;
import com.belka.core.previous_step.dto.PreviousStepDto;
import com.belka.core.utils.CompletableFutureUtil;
import com.belka.stats.models.Stats;
import com.belka.stats.services.StatsService;
import com.belka.users.models.User;
import com.belka.users.services.UserService;
import com.vdurmont.emoji.EmojiParser;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.OffsetDateTime;

/**
 * а handler that initializes the user in the system and starts the interaction
 */
@Component
@AllArgsConstructor
@Slf4j
public class StartHandler extends AbstractBelkaHandler {

    private final static String CODE = "/start";
    private final static String NEXT_HANDLER = "";
    private final static String CLASS_NAME = StartHandler.class.getSimpleName();
    private final UserService userService;
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
        String answer = EmojiParser.parseToUnicode("Hi, " + event.getUpdate().getMessage().getChat().getFirstName() + " nice to meet you" + " :blush:");
        savePreviousStep(getPreviousStep(chatId), CLASS_NAME);
        registerUser(event.getUpdate().getMessage());
        recordStats(getStats(chatId));
        return Flux.just(sendMessage(chatId, answer));
    }

    private void registerUser(Message message) {
        if (!userService.existsById(message.getChatId())) {
            Chat chat = message.getChat();
            User user = User.builder()
                    .id(chat.getId())
                    .firstname(chat.getFirstName())
                    .lastname(chat.getLastName())
                    .username(chat.getUserName())
                    .registeredAt(OffsetDateTime.now())
                    .build();
            userService.save(user);
        }
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
        Mono.fromRunnable(() -> statsService.save(stats))
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(unused -> log.info("Stats from {} have been recorded", CLASS_NAME))
                .doOnError(e -> log.error("Failed to record stats in {}: {}", CLASS_NAME, e.getMessage()))
                .subscribe();
    }
}
