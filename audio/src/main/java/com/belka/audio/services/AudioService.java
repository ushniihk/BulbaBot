package com.belka.audio.services;

import com.belka.audio.entities.AudioEntity;
import com.belka.audio.models.NotListened;
import com.belka.users.entities.UserEntity;
import org.telegram.telegrambots.meta.api.objects.Voice;

import java.time.LocalDate;
import java.util.Optional;

/**
 * service for work with audio messages
 */
public interface AudioService {

    /**
     * Saves a {@link AudioEntity audio} to the database.
     *
     * @param voice  the {@link Voice} to save
     * @param userId the ID of the user who sent the {@link Voice}
     */
    void saveVoice(Voice voice, Long userId);

    /**
     * Deletes a {@link AudioEntity audio}.
     *
     * @param fileId the ID of the {@link AudioEntity audio} to delete
     */
    void deleteAudio(String fileId);

    /**
     * Changes the status of a {@link AudioEntity audio} to public or private.
     *
     * @param flag   if true, the {@link AudioEntity audio} is public; if false, it is private
     * @param fileId the ID of the {@link AudioEntity audio}
     */
    void changeIsPrivateFlag(boolean flag, String fileId);


    /**
     * Deletes an audio from the listening list.
     *
     * @param userId the ID of the {@link UserEntity subscriber}
     * @param fileId the ID of the {@link AudioEntity audio}
     */
    void removeAudioFromListening(Long userId, String fileId);

    /**
     * Retrieves metadata about an {@link AudioEntity audio} for listening.
     *
     * @return the {@link NotListened metadata} of the {@link AudioEntity audio}
     */
    NotListened getMetaDataAudioForPull();

    /**
     * check if exist new {@link AudioEntity audio} for pulling
     *
     * @param userId {@link UserEntity subscriber's} ID
     * @return true if there are audios for pulling
     */
    boolean existAudioForUser(Long userId);

    /**
     * Checks if there are new audios for pulling for a user.
     *
     * @param userId the ID of the {@link UserEntity subscriber}
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

    /**
     * Retrieves the path to the audio file in the local database.
     *
     * @param fileId the ID of the audio file
     * @return the path to the audio file in the local database
     */
    String getPathToAudio(String fileId);
}

//todo: add the ability to get one audio that aggregates all the audios for the week/month/year