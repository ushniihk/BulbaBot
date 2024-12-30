package com.belka.QR.handlers;

import com.belka.core.handlers.AbstractBelkaHandler;
import com.belka.core.models.BelkaEvent;
import com.belka.core.previous_step.dto.PreviousStepDto;
import com.belka.stats.models.Stats;
import com.belka.stats.services.StatsService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.OffsetDateTime;

/**
 * the handler that processes the user's request to create a QR code
 */
@Component
@AllArgsConstructor
@Slf4j
public class InitialCommandHandler extends AbstractBelkaHandler {
    private final static String CODE = "/QR";
    private final static String NEXT_HANDLER = "";
    private final static String CLASS_NAME = InitialCommandHandler.class.getSimpleName();
    private final static String HEADER_1 = "write your text";
    private final StatsService statsService;

    @Override
    @Transactional
    public Flux<PartialBotApiMethod<?>> handle(BelkaEvent event) {
        return Mono.fromCallable(() -> isMatchingCommand(event, CODE))
                .filter(Boolean::booleanValue) // Continue only if the command matches
                .flatMapMany(isMatching -> handleStartCommand(event))
                .onErrorResume(e -> {
                    log.error("Error handling event in {}: {}", CLASS_NAME, e.getMessage(), e);
                    return Flux.empty();
                });
    }

    private Flux<PartialBotApiMethod<?>> handleStartCommand(BelkaEvent event) {
        log.info("Start command handling in a class {}", CLASS_NAME);
        Long chatId = event.getChatId();
        savePreviousStep(getPreviousStep(chatId), CLASS_NAME);
        recordStats(getStats(chatId));
        return Flux.just(sendMessage(chatId, HEADER_1));
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
