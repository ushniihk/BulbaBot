package com.belka.audio.utils;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * converter audio from .ogg to .wav
 */
@Component
public class OggToWavConverter {

    private static final String WAV_EXTENSION = "WAV";

    /**
     * we use ffmpeg to convert audio files from ogg format to wav format, first we need to install a server
     *
     * @param fileOgg path to file in DB
     */
    public void convert(String fileOgg) {
        // trim the extension .ogg in the string and change it to .wav and make sample rate is 16000 (we need this rate for cloud processing)
        String fileWav = fileOgg.substring(0, fileOgg.length() - 3) + WAV_EXTENSION;
        String cmd = String.format("ffmpeg -i %s -ar 16000 %s", fileOgg, fileWav);
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            p.waitFor();
            Files.delete(Path.of(fileOgg));
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

