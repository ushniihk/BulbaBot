package com.belka.audio.services;

import com.belka.audio.entities.AudioEntity;
import com.belka.audio.models.NotListened;
import com.belka.audio.repositories.AudioRepository;
import com.belka.audio.repositories.NotListenedRepository;
import com.belka.core.converters.ConverterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AudioDatabaseServiceImpl implements AudioDatabaseService {
    private final AudioRepository audioRepository;
    private final NotListenedRepository notListenedRepository;
    private final ConverterService converterService;

    @Override
    public void saveAudio(String fileId, Long userId, String text) {
        AudioEntity entity = AudioEntity.builder()
                .id(fileId)
                .date(LocalDate.now())
                .userId(userId)
                .text(text)
                .build();
        audioRepository.save(entity);
    }

    @Override
    public void deleteAudioFromDB(String fileId) {
        audioRepository.deleteById(fileId);
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

    @Override
    public NotListened getMetaDataAudioForPull() {
        return converterService.convertTo(NotListened.class, notListenedRepository.getOldestAudio());
    }

    @Override
    public void changeStatus() {
        audioRepository.fillNotListened();
        log.info("audios added to listening");
    }

    public Map<Long, List<AudioEntity>> getUserAudiosByDate(LocalDate date) {
        return audioRepository.findByDate(date).stream()
                .collect(Collectors.groupingBy(AudioEntity::getUserId));
    }

    public void saveAudio(AudioEntity audioEntity) {
        audioRepository.save(audioEntity);
    }

    public void deleteAudios(List<AudioEntity> audios) {
        audioRepository.deleteAll(audios);
    }
}
