package com.belka.audio.services;

import com.belka.audio.entities.AudioEntity;
import com.belka.audio.models.NotListened;
import com.belka.audio.repositories.AudioRepository;
import com.belka.audio.repositories.NotListenedRepository;
import com.belka.audio.utils.OggToWavConverter;
import com.belka.core.converters.ConverterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.objects.Voice;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AudioServiceImpl implements AudioService {
    final static String OGG = ".ogg";
    final static String WAV = ".WAV";
    private final RestTemplate restTemplate;
    private final HttpHeaders headers;
    private final AudioRepository audioRepository;
    private final NotListenedRepository notListenedRepository;
    private final ConverterService converterService;
    private final OggToWavConverter oggToWavConverter;
    @Value("${bot.audio.path}")
    private String pathToAudio;
    @Value("${bot.token}")
    private String token;
    @Value("${service.file_info.uri}")
    private String fileInfoUri;
    @Value("${service.file_storage.uri}")
    private String fileStorageUri;
    @Value("${bot.audio.speech_recognition.uri}")
    private String speechRecognitionServiceUrl;

    @Override
    @Transactional
    public void saveVoice(Voice voice, Long userId) {
        String fileId = voice.getFileId();
        try {
            // Save an audio file to local storage
            saveAudioToLocalStorage(fileId);
            String text = analyzeVoice(fileId);
            saveAudioToDB(fileId, userId, text);
        } catch (Exception e) {
            log.error("Error processing voice data", e);
            throw new RuntimeException("Error processing voice data", e);
        }
    }


    @Transactional
    public void saveAudioToLocalStorage(String fileId) {
        try {
            // Downloading a file and saving it to local storage
            Path localPath = downloadAndSaveVoiceFile(fileId);

            // Convert OGG to WAV
            convertOggToWav(localPath);

        } catch (Exception e) {
            deleteAudio(fileId);
            throw new RuntimeException("Error processing voice data", e);
        }
    }

    private Path downloadAndSaveVoiceFile(String fileId) throws IOException {
        ResponseEntity<String> response = getFilePath(fileId);
        byte[] downloadFile = downloadFile(getFilePath(response));
        Path filePath = Paths.get(pathToAudio, fileId + OGG);
        Files.write(filePath, downloadFile);
        return filePath;
    }

    private void convertOggToWav(Path filePath) {
        try {
            oggToWavConverter.convert(filePath.toString());
        } catch (Exception e) {
            log.error("Error converting OGG to WAV", e);
            throw new RuntimeException("Conversion failed", e);
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
            deleteAudioFromLocalStorage(fileId);
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

    @Override
    public String getPathToAudio(String fileId) {
        return pathToAudio + fileId + WAV;
    }

    private void deleteAudioFromLocalStorage(String fileId) {
        Path path = Paths.get(pathToAudio, fileId + WAV);
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.error("Error deleting file", e);
        }
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

    private ResponseEntity<String> getFilePath(String fileId) {
        HttpEntity<String> request = new HttpEntity<>(headers);
        return restTemplate.exchange(
                fileInfoUri,
                HttpMethod.GET,
                request,
                String.class,
                token,
                fileId
        );
    }

    private String getFilePath(ResponseEntity<String> response) {
        JSONObject jsonObject = new JSONObject(response.getBody());
        return String.valueOf(jsonObject
                .getJSONObject("result")
                .getString("file_path"));
    }

    private byte[] downloadFile(String filePath) {
        String fullUri = fileStorageUri
                .replace("{token}", token)
                .replace("{filePath}", filePath);
        URL urlObj;
        try {
            urlObj = new URL(fullUri);
        } catch (MalformedURLException e) {
            throw new RuntimeException("bad URL passed, " + e.getMessage());
        }

        try (InputStream is = urlObj.openStream()) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[1024];
            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            return buffer.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(urlObj.toExternalForm(), e);
        }
    }

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void changeStatus() {
        audioRepository.fillNotListened();
        log.info("audios added to listening");
    }

    private String analyzeVoice(String fileName) {
        String url = speechRecognitionServiceUrl + fileName + ".WAV";
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, null, String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            } else {
                throw new RuntimeException("Voice analysis failed: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error analyzing voice", e);
            throw new RuntimeException("Error analyzing voice: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void concatenateAudios(List<String> audioIds, String outputFileId) {
        if (audioIds == null || audioIds.isEmpty()) {
            log.info("No audio files to concatenate for output: {}", outputFileId);
            return;
        }

        Path outputWavPath = Path.of(pathToAudio, outputFileId + WAV);

        try {
            // Sort the files in alphabetical order. Because now file naming is based on creation time
            // and accordingly alphabetical order shows older records first
            List<File> audioFiles = audioIds.stream()
                    .sorted() // Sorting by file name
                    .map(id -> new File(pathToAudio + id + WAV))
                    .toList();

            if (audioFiles.isEmpty()) {
                log.warn("No valid audio files found for IDs: {}", audioIds);
                return;
            }

            // Getting the first audio stream
            AudioInputStream appendedFiles = AudioSystem.getAudioInputStream(audioFiles.get(0));

            // Add the rest of the audio streams
            for (int i = 1; i < audioFiles.size(); i++) {
                File audioFile = audioFiles.get(i);
                AudioInputStream nextClip = AudioSystem.getAudioInputStream(audioFile);
                appendedFiles = new AudioInputStream(
                        new SequenceInputStream(appendedFiles, nextClip),
                        appendedFiles.getFormat(),
                        appendedFiles.getFrameLength() + nextClip.getFrameLength()
                );
            }

            // Save the merged file
            AudioSystem.write(appendedFiles, AudioFileFormat.Type.WAVE, outputWavPath.toFile());
            log.info("Concatenated audio saved to: {}", outputWavPath);

            // Delete source files after successful merge
            for (File audioFile : audioFiles) {
                Files.deleteIfExists(audioFile.toPath());
            }
        } catch (Exception e) {
            log.error("Error concatenating audio files for IDs: {}", audioIds, e);
            throw new RuntimeException("Failed to concatenate audio files", e);
        }
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
                concatenateAudios(audioIds, mergedFileId);

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

