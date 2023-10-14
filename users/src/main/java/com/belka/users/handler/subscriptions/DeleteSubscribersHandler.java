package com.belka.users.handler.subscriptions;

import com.belka.core.handlers.AbstractBelkaHandler;
import com.belka.core.handlers.BelkaEvent;
import com.belka.core.previous_step.dto.PreviousStepDto;
import com.belka.core.previous_step.service.PreviousService;
import com.belka.stats.StatsDto;
import com.belka.stats.service.StatsService;
import com.belka.users.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import reactor.core.publisher.Flux;

import java.time.OffsetDateTime;
import java.util.concurrent.CompletableFuture;

import static com.belka.users.handler.subscriptions.ChooseWhoToUnsubscribeFromHandler.NO_BUTTON;
import static com.belka.users.handler.subscriptions.ChooseWhoToUnsubscribeFromHandler.YES_BUTTON;
import static com.belka.users.handler.subscriptions.UnsubscribeHandler.PREFIX_FOR_UNSUBSCRIBE_CALLBACK;

@AllArgsConstructor
@Component
public class DeleteSubscribersHandler extends AbstractBelkaHandler {
    public final static String CODE = "/Choose Who To Unsubscribe From";
    private final static String NEXT_HANDLER = "";
    private final static String PREVIOUS_HANDLER = ChooseWhoToUnsubscribeFromHandler.CODE;
    private final static String UNSUBSCRIBE_ANSWER = "unsubscribed";
    private final static String EVERYONE_STAYS_ANSWER = "Ok, everyone stays";
    private final UserService userService;
    private final PreviousService previousService;
    private final StatsService statsService;

    @Override
    @Transactional
    public Flux<PartialBotApiMethod<?>> handle(BelkaEvent event) {
        CompletableFuture<Flux<PartialBotApiMethod<?>>> future = CompletableFuture.supplyAsync(() -> {
            if (event.getPrevious_step().equals(PREVIOUS_HANDLER) && event.isHasCallbackQuery() &&
                    event.getData().startsWith(PREFIX_FOR_UNSUBSCRIBE_CALLBACK + YES_BUTTON)) {

                Long chatId = event.getChatId();
                savePreviousAndStats(chatId);
                Long producerId = Long.valueOf(event.getData().substring((PREFIX_FOR_UNSUBSCRIBE_CALLBACK + YES_BUTTON).length()));
                userService.toUnsubscribe(chatId, producerId);
                SendMessage message = sendMessage(chatId, UNSUBSCRIBE_ANSWER);
                message.setReplyToMessageId(event.getUpdate().getCallbackQuery().getMessage().getMessageId());
                return Flux.just(editMessage(message, UNSUBSCRIBE_ANSWER));
            } else if (event.getPrevious_step().equals(PREVIOUS_HANDLER) && event.isHasCallbackQuery() &&
                    event.getData().startsWith(PREFIX_FOR_UNSUBSCRIBE_CALLBACK + NO_BUTTON)) {

                Long chatId = event.getChatId();
                savePreviousAndStats(chatId);
                SendMessage message = sendMessage(chatId, EVERYONE_STAYS_ANSWER);
                message.setReplyToMessageId(event.getUpdate().getCallbackQuery().getMessage().getMessageId());
                return Flux.just(editMessage(message, EVERYONE_STAYS_ANSWER));
            }
            return Flux.empty();
        });
        return getCompleteFuture(future, event.getChatId());
    }

    private void savePreviousAndStats(Long chatId) {
        previousService.save(PreviousStepDto.builder()
                .previousStep(CODE)
                .nextStep(NEXT_HANDLER)
                .userId(chatId)
                .build());
        statsService.save(StatsDto.builder()
                .userId(chatId)
                .handlerCode(CODE)
                .requestTime(OffsetDateTime.now())
                .build());
    }
}
