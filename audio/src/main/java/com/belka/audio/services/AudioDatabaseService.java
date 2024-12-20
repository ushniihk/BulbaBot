package com.belka.audio.services;

import com.belka.audio.entities.AudioEntity;
import com.belka.audio.models.NotListened;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for working with audio files in the database.
 */
public interface AudioDatabaseService {
    /**
     * Saves an audio file to the database.
     *
     * @param fileId the ID of the audio file
     * @param userId the ID of the user who sent the audio
     * @param text   the text of the audio
     */
    void saveAudio(String fileId, Long userId, String text);

    /**
     * Saves an audio file to the database.
     *
     * @param audioEntity the audio entity to save
     */
    void saveAudio(AudioEntity audioEntity);

    /**
     * Deletes an audio file from the database.
     *
     * @param fileId the ID of the audio file
     */

    void deleteAudioFromDB(String fileId);

    /**
     * Changes the status of a {@link AudioEntity} to public or private.
     *
     * @param flag   if true, the {@link AudioEntity} is public; if false, it is private
     * @param fileId the ID of the {@link AudioEntity}
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
     * Checks if there are new audios for pulling.
     *
     * @param userId the ID of the subscriber
     * @return true if there are audios for pulling
     */
    boolean existAudioForUser(Long userId);

    /**
     * Checks if an audio file exists from a user on a specific date.
     *
     * @param userId the ID of the user
     * @param date   the date
     * @return true if the audio file exists
     */

    boolean existsByUserIdAndDate(Long userId, LocalDate date);

    /**
     * Retrieves the ID of a user's audio for a specific day.
     *
     * @param userId the ID of the user
     * @param date   the date
     * @return the ID of the audio file
     */

    Optional<String> getFileId(Long userId, LocalDate date);

    /**
     * Retrieves metadata about an audio for listening.
     *
     * @return the metadata of the audio
     */

    NotListened getMetaDataAudioForPull();

    /**
     * Changes the file status to public or private
     */

    void changeStatus();

    /**
     * Retrieves all audio files from an every user by date.
     *
     * @param date the specific date
     * @return map of user ID and list of audio files from that user
     */

    Map<Long, List<AudioEntity>> getUserAudiosByDate(LocalDate date);

    /**
     * Deletes a list of audio files from the database.
     *
     * @param audios the list of audio files to delete
     */

    void deleteAudios(List<AudioEntity> audios);

}
