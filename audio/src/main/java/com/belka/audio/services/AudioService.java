package com.belka.audio.services;

import org.telegram.telegrambots.meta.api.objects.Voice;

import java.util.Collection;

/**
 * service for work with audio messages
 */
public interface AudioService {
    /**
     * save {@link Voice voice} to DB
     *
     * @param voice  {@link Voice voice}
     * @param userId ID of the user who sent this {@link Voice voice}
     */
    void saveVoice(Voice voice, Long userId);

    /**
     * get path to Voice in local DB
     *
     * @param fileId voice's ID
     * @return path to Voice in local DB
     */
    String getPathToAudio(String fileId);

    /**
     * delete chosen Voice
     *
     * @param fileId Voice's ID
     */
    void deleteVoice(String fileId);

    /**
     * change voice status on public or private
     *
     * @param fileId Voice's ID
     * @param flag   if the voice status is private, the flag is false
     */
    void changeIsPrivateFlag(boolean flag, String fileId);

    /**
     * get all audio IDs by user
     *
     * @param userId ID of the user who sent this {@link Voice voices}
     */
    Collection<String> getAudiosIDbyUser(Long userId);
}