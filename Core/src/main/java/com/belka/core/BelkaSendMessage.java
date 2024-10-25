package com.belka.core;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendAudio;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.io.File;

/**
 * Utility class for creating and sending {@link PartialBotApiMethod} messages to the user.
 */
@Component
public class BelkaSendMessage {

    /**
     * Creates and sends a {@link SendMessage} to the user.
     *
     * @param chatId the user's chat ID
     * @param answer the message text to send
     * @return the {@link SendMessage} object
     */
    public SendMessage sendMessage(Long chatId, String answer) {
        return SendMessage.builder()
                .chatId(chatId)
                .text(answer)
                .build();
    }

    /**
     * Takes a picture from the url and sends it to the user
     *
     * @param url    the URL of the picture
     * @param chatId the user's chatId
     * @return the {@link PartialBotApiMethod} object for sending the photo
     */
    public PartialBotApiMethod<?> sendImageFromUrl(String url, Long chatId) {
        // Create send method
        SendPhoto sendPhotoRequest = new SendPhoto();
        // Set destination chat id
        sendPhotoRequest.setChatId(chatId);
        // Set the photo url as a simple photo
        sendPhotoRequest.setPhoto(new InputFile(url));
        return sendPhotoRequest;
    }

    /**
     * takes an audio file from local storage to the user.
     *
     * @param path   the path to the audio file
     * @param chatId the user's chat ID
     * @return the {@link PartialBotApiMethod} object for sending the audio
     */
    public PartialBotApiMethod<?> sendAudioFromLocalStorage(String path, Long chatId) {
        // Create send method
        File audioFile = new File(path);
        SendAudio sendAudioRequest = new SendAudio();
        // Set destination chat id
        sendAudioRequest.setChatId(chatId);
        // Set the audio path as a simple audio
        sendAudioRequest.setAudio(new InputFile(audioFile, audioFile.getName()));
        return sendAudioRequest;
    }

    /**
     * Edits an existing message.
     *
     * @param message the original {@link SendMessage} object
     * @param text    the new text for the message
     * @return the {@link PartialBotApiMethod} object for editing the message
     */
    public PartialBotApiMethod<?> editMessage(SendMessage message, String text) {
        new EditMessageText();
        return EditMessageText.builder()
                .chatId(message.getChatId())
                .messageId(message.getReplyToMessageId())
                .text(text)
                .replyMarkup((InlineKeyboardMarkup) message.getReplyMarkup())
                .build();
    }
}
