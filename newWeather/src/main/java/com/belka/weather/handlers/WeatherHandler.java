package com.belka.weather.handlers;

import com.belka.core.handlers.AbstractBelkaHandler;
import com.belka.core.models.BelkaEvent;
import com.belka.core.previous_step.dto.PreviousStepDto;
import com.belka.stats.models.Stats;
import com.belka.stats.services.StatsService;
import com.belka.weather.services.weather.WeatherService;
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
 * the handler that processes the weather request
 */
@Component
@AllArgsConstructor
@Slf4j
public class WeatherHandler extends AbstractBelkaHandler {

    private final static String CODE = "/weather";
    private final static String NEXT_HANDLER = "";
    private final static String CLASS_NAME = WeatherHandler.class.getSimpleName();
    private final WeatherService weatherService;
    private final StatsService statsService;

    @Override
    @Transactional
    public Flux<PartialBotApiMethod<?>> handle(BelkaEvent event) {
        return Mono.fromCallable(() -> isMatchingCommand(event, CODE))
                .filter(isMatching -> isMatching) // Continue only if the command matches
                .flatMapMany(isMatching -> handleCommand(event))
                .onErrorResume(e -> {
                    log.error("Error handling event in {}: {}", CLASS_NAME, e.getMessage(), e);
                    return Flux.empty();
                });
    }

    private Flux<PartialBotApiMethod<?>> handleCommand(BelkaEvent event) {
        Long chatId = event.getChatId();
        savePreviousStep(getPreviousStep(chatId), CLASS_NAME);
        recordStats(getStats(chatId));
        return Flux.just(sendMessage(chatId, weatherService.getWeatherResponse(weatherService.findCity())));
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
