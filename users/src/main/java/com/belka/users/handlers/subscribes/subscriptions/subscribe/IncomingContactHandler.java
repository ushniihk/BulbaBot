package com.belka.users.handlers.subscribes.subscriptions.subscribe;

import com.belka.core.handlers.AbstractBelkaHandler;
import com.belka.core.models.BelkaEvent;
import com.belka.core.previous_step.dto.PreviousStepDto;
import com.belka.core.utils.CompletableFutureUtil;
import com.belka.stats.models.Stats;
import com.belka.stats.services.StatsService;
import com.belka.users.services.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Contact;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.OffsetDateTime;

@Component
@AllArgsConstructor
@Slf4j
public class IncomingContactHandler extends AbstractBelkaHandler {
    public final static String CODE = "Incoming Contact";
    private final static String NEXT_HANDLER = "";
    private final static String CLASS_NAME = IncomingContactHandler.class.getSimpleName();
    private final static String SUCCESSFULLY_ANSWER = "subscription is issued";
    private final static String FAILED_ANSWER = "this user is not registered";
    private final StatsService statsService;
    private final UserService userService;
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
        log.info("Start command handling in a class {}", CLASS_NAME);
        Contact contact = event.getUpdate().getMessage().getContact();
        if (userService.existsById(contact.getUserId())) {
            userService.toSubscribe(event.getChatId(), contact.getUserId());
            Long chatId = event.getChatId();
            savePreviousStep(getPreviousStep(chatId), CLASS_NAME);
            recordStats(getStats(chatId));

            return Flux.just(sendMessage(chatId, SUCCESSFULLY_ANSWER));
        }
        return Flux.just(sendMessage(event.getChatId(), FAILED_ANSWER));
    }

    private boolean isSubscribeCommand(BelkaEvent event) {
        return event.isHasMessage() && event.getUpdate().getMessage().hasContact();
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
