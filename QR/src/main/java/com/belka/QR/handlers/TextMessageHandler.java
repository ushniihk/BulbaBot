package com.belka.QR.handlers;

import com.belka.QR.services.QRService;
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
import java.util.concurrent.CompletableFuture;

/**
 * the handler that processes the user's request to create a QR code
 */
@Component
@AllArgsConstructor
@Slf4j
public class TextMessageHandler extends AbstractBelkaHandler {
    private final static String CODE = "/QR";
    private final static String CLASS_NAME = TextMessageHandler.class.getSimpleName();
    private final static String EXIT_CODE = "/send QR";
    private final QRService qrService;
    private final StatsService statsService;

    @Override
    @Transactional
    public Flux<PartialBotApiMethod<?>> handle(BelkaEvent event) {
        CompletableFuture<Flux<PartialBotApiMethod<?>>> future = CompletableFuture.supplyAsync(() -> {
            try {
                if (isMatchingCommand(event)) {
                    return handleTextMessage(event);
                }
            } catch (Exception e) {
                log.error("Error handling event in {}: {}", CLASS_NAME, e.getMessage(), e);
            }
            return Flux.empty();
        });
        return getCompleteFuture(future, event.getChatId());
    }

    private boolean isMatchingCommand(BelkaEvent event) {
        return event.isHasMessage() && event.isHasText() && event.getPrevious_step().equals(CODE);
    }

    private Flux<PartialBotApiMethod<?>> handleTextMessage(BelkaEvent event) {
        Long chatId = event.getChatId();
        savePreviousStep(getPreviousStep(chatId), CLASS_NAME);
        recordStats(getStats(chatId));
        return Flux.just(sendImageFromUrl(qrService.getQRLink(event.getText()), chatId));
    }

    private PreviousStepDto getPreviousStep(Long chatId) {
        return PreviousStepDto.builder()
                .previousStep(EXIT_CODE)
                .userId(chatId)
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
