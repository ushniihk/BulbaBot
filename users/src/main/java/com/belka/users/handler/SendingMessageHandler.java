package com.belka.users.handler;

import com.belka.core.BelkaSendMessage;
import com.belka.core.handlers.BelkaEvent;
import com.belka.core.handlers.BelkaHandler;
import com.belka.core.previous_step.dto.PreviousStepDto;
import com.belka.core.previous_step.service.PreviousService;
import com.belka.stats.StatsDto;
import com.belka.stats.service.StatsService;
import com.belka.users.UserConfig;
import com.belka.users.service.UserService;
import com.vdurmont.emoji.EmojiParser;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * the handler that processes the request to create a mailing list
 */
@Component
@AllArgsConstructor
public class SendingMessageHandler implements BelkaHandler {

    private final static String CODE = "message has been sent";
    private final static String PREVIOUS_CODE = "/send";
    private final PreviousService previousService;
    private final UserService userService;
    private final UserConfig userConfig;
    private final StatsService statsService;
    private final BelkaSendMessage belkaSendMessage;

    @Override
    public Flux<PartialBotApiMethod<?>> handle(BelkaEvent event) {
        if (event.isHasText()
                && event.getPrevious_step().equals(PREVIOUS_CODE)
                && userConfig.getBotOwner().equals(event.getChatId())) {
            Long chatId = event.getChatId();
            previousService.save(PreviousStepDto.builder()
                    .previousStep(CODE)
                    .userId(chatId)
                    .build());

            String textToSend = EmojiParser.parseToUnicode(event.getText());

            statsService.save(StatsDto.builder()
                    .userId(event.getChatId())
                    .handlerCode(CODE)
                    .requestTime(LocalDateTime.now())
                    .build());
            return Flux.fromIterable(userService.getAll())
                    .flatMap(userDto -> Mono.just(belkaSendMessage.sendMessage(userDto.getId(), textToSend)));
        }
        return null;
    }
}
