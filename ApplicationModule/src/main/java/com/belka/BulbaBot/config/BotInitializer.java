package com.belka.BulbaBot.config;

import com.belka.BulbaBot.service.TelegramBot;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * initialize telegram bot
 */
@Component
@AllArgsConstructor
@Slf4j
public class BotInitializer {
    private final TelegramBot bot;
    private final TelegramBotsApi telegramBotsApi;

    @EventListener({ContextRefreshedEvent.class})
    public void init() {
        try {
            telegramBotsApi.registerBot(bot);
        } catch (TelegramApiException e) {
            log.error("Error registering bot", e);
        }
    }
}
