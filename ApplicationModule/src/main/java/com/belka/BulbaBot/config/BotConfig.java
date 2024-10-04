package com.belka.BulbaBot.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.bots.DefaultBotOptions;

@Configuration
@Getter
@EntityScan("com.belka.BulbaBot.model")
public class BotConfig {

    @Value("${bot.name}")
    private String botName;
    @Value("${bot.token}")
    private String token;

    @Bean
    public DefaultBotOptions defaultBotOptions() {
        return new DefaultBotOptions();
    }
}
