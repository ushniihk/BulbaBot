package com.belka.audio.handlers;

import com.belka.audio.models.NotListened;
import com.belka.audio.services.AudioCalendarService;
import com.belka.audio.services.AudioService;
import com.belka.core.handlers.AbstractBelkaHandler;
import com.belka.core.models.BelkaEvent;
import com.belka.core.previous_step.dto.PreviousStepDto;
import com.belka.core.utils.CompletableFutureUtil;
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

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Component
@AllArgsConstructor
@Slf4j
public class PullAudioHandler extends AbstractBelkaHandler {
    final static String CODE = "/pull audio";
    private final static String NEXT_HANDLER = "";
    private final static String CLASS_NAME = PullAudioHandler.class.getSimpleName();
    private final static String NO_AUDIO_ANSWER = "there are no new audios for you";
    private final AudioService audioService;
    private final StatsService statsService;
    private final CompletableFutureUtil completableFutureUtil;

    @Override
    @Transactional
    public Flux<PartialBotApiMethod<?>> handle(BelkaEvent event) {
        return completableFutureUtil.supplyAsync(() -> {
            if (isMatchingCommand(event, CODE)) {
                return handleCalendarCommand(event);
            }
            if (isCalendarCallback(event)) {
                return handleCalendarCallback(event);
            }
            return Flux.empty();
        }, CLASS_NAME).join();
    }

    private Flux<PartialBotApiMethod<?>> handleCalendarCommand(BelkaEvent event) {
        log.info("Start command handling in a class {}", CLASS_NAME);
        Long chatId = event.getChatId();
        if (!audioService.existAudioForUser(chatId)) {
            return Flux.just(sendMessage(chatId, "there are no new audios for you"));
        }
        NotListened notListenedAudio = audioService.getMetaDataAudioForPull();
        String fileId = notListenedAudio.getAudioId();
        audioService.removeAudioFromListening(notListenedAudio.getSubscriber(), fileId);

        savePreviousAndStats(chatId);

        return Flux.just(sendAudioFromLocalStorage(audioService.getPathToAudio(fileId), chatId));
    }

    private Flux<PartialBotApiMethod<?>> handleCalendarCallback(BelkaEvent event) {
        log.info("Start command handling in a class {}", CLASS_NAME);
        Long chatId = event.getChatId();
        String[] data = event.getData().split("\\.");
        LocalDate date = LocalDate.of(Integer.parseInt(data[1]), Integer.parseInt(data[2]) + 1, Integer.parseInt(data[3]));
        String fileId = audioService.getFileId(chatId, date).orElse("");

        savePreviousAndStats(chatId);

        if (fileId.isEmpty()) {
            return Flux.just(sendMessage(chatId, NO_AUDIO_ANSWER));
        }
        return Flux.just(sendAudioFromLocalStorage(audioService.getPathToAudio(fileId), chatId));
    }

    private boolean isCalendarCallback(BelkaEvent event) {
        return event.isHasCallbackQuery() && event.getData().startsWith(AudioCalendarService.DATA_AUDIO_CODE);
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
