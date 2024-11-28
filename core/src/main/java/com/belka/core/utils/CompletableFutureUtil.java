package com.belka.core.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import reactor.core.publisher.Flux;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@Slf4j
@Component
public class CompletableFutureUtil {
    public CompletableFuture<Flux<PartialBotApiMethod<?>>> supplyAsync(Supplier<Flux<PartialBotApiMethod<?>>> supplier, String className) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return supplier.get();
            } catch (Exception e) {
                log.error("Error handling event in {}: {}", className, e.getMessage(), e);
                return Flux.empty();
            }
        });
    }
}