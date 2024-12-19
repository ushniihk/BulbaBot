package com.belka.audio.services;

import com.belka.audio.entities.AudioEntity;
import com.belka.audio.models.NotListened;
import com.belka.audio.repositories.AudioRepository;
import com.belka.audio.repositories.NotListenedRepository;
import com.belka.core.converters.ConverterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.Voice;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AudioServiceImpl implements AudioService {
    private final AudioRepository audioRepository;
    private final NotListenedRepository notListenedRepository;
    private final ConverterService converterService;
    private final FileStorageService fileStorageService;
    private final AudioRecognitionService audioRecognitionService;

    @Override
    @Transactional
    public void saveVoice(Voice voice, Long userId) {
        String fileId = voice.getFileId();
        try {
            // Save an audio file to local storage
            fileStorageService.saveAudioToLocalStorage(fileId);
            String text = audioRecognitionService.analyzeVoice(fileId);
            saveAudioToDB(fileId, userId, text);
        } catch (Exception e) {
            log.error("Error processing voice data", e);
            throw new RuntimeException("Error processing voice data", e);
        }
    }

    private void saveAudioToDB(String fileId, Long userId, String text) {
        AudioEntity entity = AudioEntity.builder()
                .id(fileId)
                .date(LocalDate.now())
                .userId(userId)
                .text(text)
                .build();
        audioRepository.save(entity);
    }

    @Override
    @Transactional
    public void deleteAudio(String fileId) {
        try {
            fileStorageService.deleteAudioFromLocalStorage(fileId);
            deleteAudioFromDB(fileId);
        } catch (Exception e) {
            log.error("Error deleting file", e);
        }
    }

    @Override
    @Transactional
    public void changeIsPrivateFlag(boolean flag, String fileId) {
        audioRepository.changeIsPrivateFlag(flag, fileId);
    }

    @Override
    public void removeAudioFromListening(Long userId, String fileId) {
        audioRepository.deleteFromNotListened(userId, fileId);
    }

    private void deleteAudioFromDB(String fileId) {
        audioRepository.deleteById(fileId);
    }

    @Override
    public NotListened getMetaDataAudioForPull() {
        return converterService.convertTo(NotListened.class, notListenedRepository.getOldestAudio());
    }

    @Override
    public boolean existAudioForUser(Long userId) {
        return notListenedRepository.existsBySubscriber(userId);
    }

    @Override
    public boolean existsByUserIdAndDate(Long userId, LocalDate date) {
        return audioRepository.existsByDateAndUserId(date, userId);
    }

    @Override
    public Optional<String> getFileId(Long userId, LocalDate date) {
        return audioRepository.getIdByDateAndUserId(date, userId);
    }

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void changeStatus() {
        audioRepository.fillNotListened();
        log.info("audios added to listening");
    }

    @Scheduled(cron = "0 0 2 * * *") // We start every day at 2:00 in the morning.
    @Transactional
    public void mergeDailyAudios() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        log.info("Starting daily audio merge for date: {}", yesterday);

        // Get all audio recordings for yesterday, grouped by userId
        Map<Long, List<AudioEntity>> userAudios = audioRepository.findByDate(yesterday).stream()
                .collect(Collectors.groupingBy(AudioEntity::getUserId));

        for (Map.Entry<Long, List<AudioEntity>> entry : userAudios.entrySet()) {
            Long userId = entry.getKey();
            List<AudioEntity> audios = entry.getValue();

            // Create a list of files to be glued together
            List<String> audioIds = audios.stream()
                    .map(AudioEntity::getId)
                    .toList();

            // Merging text data
            String mergedText = audios.stream()
                    .map(AudioEntity::getText)
                    .filter(text -> text != null && !text.isEmpty()) // Remove empty values
                    .collect(Collectors.joining(" ")); // Merge with a space


            // Unique ID for the final file
            String mergedFileId = userId + "_" + yesterday;

            // Merge files
            try {
                fileStorageService.concatenateAudios(audioIds, mergedFileId);

                // Save the final record to the database
                AudioEntity mergedAudio = AudioEntity.builder()
                        .id(mergedFileId)
                        .date(yesterday)
                        .userId(userId)
                        .text(mergedText)
                        .build();

                audioRepository.save(mergedAudio);

                // Delete source records from the database
                audioRepository.deleteAll(audios);

                log.info("Successfully merged audios for userId: {} into file: {}", userId, mergedFileId);
            } catch (Exception e) {
                log.error("Failed to merge audios for userId: {}", userId, e);
            }
        }

        log.info("Daily audio merge completed.");
    }
}
//todo:how to make analyzeVoice run only when external service/speech_recognize module is running?;
//todo: think how to show text data to user
//todo: we can make also audio from a week or a month
//todo: do refactor of all speech_recognize module, make it pretty and clean
//todo: do refactor of AudioServiceImpl, make it pretty and clean
//todo: Improve the analyzeVoice method to check if the external service is available before processing. It's discussed
//todo: add /healt endpoint to check if the recognition service is available, if not pass "" text;
//todo: now when we want to pull audio before they merged we get the first audio, but it's not properly, we need to think about how to fix it
