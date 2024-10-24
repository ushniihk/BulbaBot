package com.belka.audio.services;

import com.belka.audio.models.NotListened;
import org.telegram.telegrambots.meta.api.objects.Voice;

import java.time.LocalDate;
import java.util.Optional;

/**
 * service for work with audio messages
 */
public interface AudioService {

    /**
     * Saves a {@link Voice} to the database.
     *
     * @param voice  the {@link Voice} to save
     * @param userId the ID of the user who sent the {@link Voice}
     */
    void saveVoice(Voice voice, Long userId);


    /**
     * Retrieves the path to a {@link Voice} in the local database.
     *
     * @param fileId the ID of the {@link Voice}
     * @return the path to the {@link Voice} in the local database
     */
    String getPathToAudio(String fileId);

    /**
     * Deletes a {@link Voice}.
     *
     * @param fileId the ID of the {@link Voice} to delete
     */
    void deleteVoice(String fileId);

    /**
     * Changes the status of a {@link Voice} to public or private.
     *
     * @param flag   if true, the {@link Voice} is public; if false, it is private
     * @param fileId the ID of the {@link Voice}
     */
    void changeIsPrivateFlag(boolean flag, String fileId);


    /**
     * Deletes an audio from the listening list.
     *
     * @param userId the ID of the subscriber
     * @param fileId the ID of the audio
     */
    void removeAudioFromListening(Long userId, String fileId);

    /**
     * Retrieves metadata about an audio for listening.
     *
     * @return the metadata of the audio
     */
    NotListened getMetaDataAudioForPull();

    /**
     * check if exist new audios for pulling
     *
     * @param userId subscriber's ID
     * @return true if there are audios for pulling
     */
    boolean existAudioForUser(Long userId);

    /**
     * Checks if there are new audios for pulling for a user.
     *
     * @param userId the ID of the subscriber
     * @return true if there are audios for pulling, false otherwise
     */
    boolean existsByUserIdAndDate(Long userId, LocalDate date);

    /**
     * Retrieves the ID of a user's audio for a specific day.
     *
     * @param userId the ID of the user
     * @param date   the date to retrieve the audio ID for
     * @return an {@link Optional} containing the audio ID if found, or an empty {@link Optional} otherwise
     */
    Optional<String> getFileId(Long userId, LocalDate date);
}