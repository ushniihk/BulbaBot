package com.belka.audio.services;

import com.belka.audio.utils.OggToWavConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {
    private final static String OGG = ".ogg";
    private final static String WAV = ".WAV";
    private final OggToWavConverter oggToWavConverter;
    private final RestTemplate restTemplate;
    private final HttpHeaders headers;
    @Value("${bot.audio.path}")
    private String pathToAudio;
    @Value("${bot.token}")
    private String token;
    @Value("${service.file_info.uri}")
    private String fileInfoUri;
    @Value("${service.file_storage.uri}")
    private String fileStorageUri;

    @Override
    @Transactional
    public void saveAudioToLocalStorage(String fileId) {
        try {
            // Downloading a file and saving it to local storage
            Path localPath = downloadAndSaveVoiceFile(fileId);

            // Convert OGG to WAV
            convertOggToWav(localPath);

        } catch (Exception e) {
            deleteAudioFromLocalStorage(fileId);
            throw new RuntimeException("Error processing voice data", e);
        }
    }

    @Override
    public void deleteAudioFromLocalStorage(String fileId) {
        Path path = Paths.get(pathToAudio, fileId + WAV);
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.error("Error deleting file", e);
        }
    }

    @Override
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

    @Override
    public String getPathToAudio(String fileId) {
        return Path.of(pathToAudio, fileId + WAV).toString();
    }

    private Path downloadAndSaveVoiceFile(String fileId) throws IOException {
        ResponseEntity<String> response = getFilePath(fileId);
        byte[] downloadFile = downloadFile(getFilePath(response));
        Path filePath = Paths.get(pathToAudio, fileId + OGG);
        Files.write(filePath, downloadFile);
        return filePath;
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

    private void convertOggToWav(Path filePath) {
        try {
            oggToWavConverter.convert(filePath.toString());
        } catch (Exception e) {
            log.error("Error converting OGG to WAV", e);
            throw new RuntimeException("Conversion failed", e);
        }
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
}
