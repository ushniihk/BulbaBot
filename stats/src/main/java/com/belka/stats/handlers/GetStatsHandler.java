package com.belka.stats.handlers;

import com.belka.core.handlers.AbstractBelkaHandler;
import com.belka.core.models.BelkaEvent;
import com.belka.core.previous_step.dto.PreviousStepDto;
import com.belka.core.utils.CompletableFutureUtil;
import com.belka.stats.configs.StatsConfig;
import com.belka.stats.models.Stats;
import com.belka.stats.services.StatsService;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.OffsetDateTime;

import static com.belka.stats.handlers.StatsStartHandler.*;

/**
 * handle all the buttons selected to view the stats
 */
@Component
@AllArgsConstructor
@Setter
@Slf4j
public class GetStatsHandler extends AbstractBelkaHandler {
    final static String CODE = "get stats";
    private final static String NEXT_HANDLER = "";
    private final static String CLASS_NAME = GetStatsHandler.class.getSimpleName();
    private final static String ANSWER = "what code?";
    private final StatsService statsService;
    private final StatsConfig statsConfig;
    private final CompletableFutureUtil completableFutureUtil;

    @Override
    @Transactional
    public Flux<PartialBotApiMethod<?>> handle(BelkaEvent event) {
        return completableFutureUtil.supplyAsync(() -> {
            if (isMatchingCommand(event)) {
                return handleCommand(event);
            }
            return Flux.empty();
        }, CLASS_NAME).join();
    }

    private Flux<PartialBotApiMethod<?>> handleCommand(BelkaEvent event) {
        log.info("Start command handling in a class {}", CLASS_NAME);
        Long chatId = event.getChatId();
        switch (event.getData()) {
            case BUTTON_1 -> {
                savePreviousAndStats(chatId);
                return Flux.just(sendMessage(chatId, String.valueOf(statsService.getTotalRequests())));
            }
            case BUTTON_2 -> {
                savePreviousAndStats(chatId);
                return Flux.just(sendMessage(chatId, String.valueOf(statsService.getTotalRequestsByUser(event.getChatId()))));
            }
            case BUTTON_3 -> {
                savePreviousAndStats(chatId);
                return Flux.just(sendMessage(chatId, ANSWER));
            }
            case BUTTON_4 -> {
                savePreviousAndStats(chatId);
                return Flux.just(sendMessage(chatId, String.valueOf(statsService.getMostPopularRequest())));
            }
            case BUTTON_5 -> {
                savePreviousAndStats(chatId);
                return Flux.just(sendMessage(chatId, String.valueOf(statsService.getMostPopularRequestByUser(chatId))));
            }
            case BUTTON_6 -> {
                savePreviousAndStats(chatId);
                return Flux.just(sendMessage(chatId, String.valueOf(statsService.countAllUsers())));
            }
        }
        return Flux.empty();
    }

    private boolean isMatchingCommand(BelkaEvent event) {
        return event.getChatId().equals(statsConfig.getBotOwner()) && event.isHasCallbackQuery();
    }

    private void savePreviousAndStats(Long chatId) {
        savePreviousStep(getPreviousStep(chatId), CLASS_NAME);
        recordStats(getStats(chatId));
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
