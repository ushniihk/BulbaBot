package com.belka.BulbaBot.service;

import com.belka.BulbaBot.config.BotConfig;
import com.belka.BulbaBot.handler.HandlerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendAudio;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * building and sending messages, receiving and processing {@link Update updates}
 */
@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    private final BotConfig botConfig;
    private final HandlerService handlerService;
    private final ExecutorService executorService;

    @Autowired
    public TelegramBot(BotConfig botConfig, HandlerService handlerService, ExecutorService executorService,
                       DefaultBotOptions options) {
        super(options, botConfig.getToken());
        this.botConfig = botConfig;
        this.handlerService = handlerService;
        this.executorService = executorService;
        setCommands();

    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    /**
     * process user {@link Update telegram update} and send receive
     *
     * @param update {@link Update telegram update}
     */
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() || update.hasCallbackQuery()) {
            executorService.execute(() -> {
                handlerService.handle(update)
                        .subscribe(this::executeMessage, this::handleError);
                log.info("end of the onUpdateReceived method");
            });
        }
    }

    /**
     * shut down the ExecutorService when it's no longer needed
     */
    @PreDestroy
    public void shutDown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS))
                    log.error("ExecutorService did not terminate");
            }
        } catch (InterruptedException ie) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * send message
     *
     * @param message {@link PartialBotApiMethod message for user}
     */
    private void executeMessage(PartialBotApiMethod<?> message) {
        try {
            if (message instanceof BotApiMethod) {
                execute((BotApiMethod<?>) message);
            } else if (message instanceof SendPhoto sendPhoto) {
                execute(sendPhoto);
            } else if (message instanceof SendDocument sendDocument) {
                execute(sendDocument);
            } else if (message instanceof SendAudio sendAudio) {
                execute(sendAudio);
            }
        } catch (TelegramApiException e) {
            log.error("Error executing message", e);
        }
    }

    /**
     * set commands in the main menu
     */
    private void setCommands() {
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "get a welcome message"));
        listOfCommands.add(new BotCommand("/help", "info how to use this bot"));
        listOfCommands.add(new BotCommand("/audio", "your own audio diary"));
        listOfCommands.add(new BotCommand("/weather", "get weather"));
        listOfCommands.add(new BotCommand("/qr", "get QR code for your text"));
        listOfCommands.add(new BotCommand("/diary", "your own diary, write down and read your thoughts"));
        listOfCommands.add(new BotCommand("/subscribes", "subscribers and subscriptions"));
        try {
            this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error setting bot's command list: " + e.getMessage());
        }
    }

    /**
     * Handle errors during message execution.
     *
     * @param throwable the error that occurred
     */
    private void handleError(Throwable throwable) {
        log.error("Error handling update: {}", throwable.getMessage());
    }
}
