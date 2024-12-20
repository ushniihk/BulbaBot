package com.belka.audio.services;

import java.util.List;
/**
 * Service responsible for managing file operations such as downloading, saving, and deleting files.
 */
public interface FileStorageService {
    /**
     *  Downloads an audio file and saves it to the local storage.
     * @param fileId the ID of the audio file to download
     */
    void saveAudioToLocalStorage(String fileId);
    /**
     * Deletes an audio file from the local storage.
     * @param fileId the ID of the audio file to delete
     */
    void deleteAudioFromLocalStorage(String fileId);
    /**
     * Concatenates multiple audio files into a single audio file.
     * @param audioIds the IDs of the audio files to concatenate
     * @param outputFileId the ID of the output audio file
     */
    void concatenateAudios(List<String> audioIds, String outputFileId);

    /**
     * Retrieves the path to the audio file in the local database.
     *
     * @param fileId the ID of the audio file
     * @return the path to the audio file in the local database
     */
    String getPathToAudio(String fileId);
}
