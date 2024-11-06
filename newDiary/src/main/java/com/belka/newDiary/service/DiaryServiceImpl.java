package com.belka.newDiary.service;


import com.belka.newDiary.entity.DiaryEntity;
import com.belka.newDiary.repository.DiaryRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@Slf4j
@AllArgsConstructor
public class DiaryServiceImpl implements DiaryService {
    private final DiaryRepository repository;

    @Override
    public void addNote(Long chatID, String text) {
        LocalDate today = LocalDate.now();
        repository.getByDateAndUserId(today, chatID)
                .ifPresentOrElse(
                        existingNote -> updateExistingNoteEntity(existingNote, text),
                        () -> saveNewNoteEntity(chatID, text, today)
                );
    }

    @Override
    @Cacheable(value = "notes", key = "#date.toString() + #chatID")
    public String getNote(LocalDate date, Long chatID) {
        return repository.findNoteByUserIdAndDate(chatID, date)
                .orElse("No note found for this date");
    }

    @Override
    public boolean existsByUserIdAndDate(Long userId, LocalDate date) {
        return repository.existsByUserIdAndDate(userId, date);
    }

    private void updateExistingNoteEntity(DiaryEntity entity, String text) {
        String newNote = entity.getNote() + "\n" + text;
        entity.setNote(newNote);
        repository.save(entity);
        log.info("Updated existing note for userId: {}, date: {}", entity.getUserId(), entity.getDate());
    }

    private void saveNewNoteEntity(Long chatID, String text, LocalDate date) {
        DiaryEntity entity = DiaryEntity.builder()
                .date(date)
                .userId(chatID)
                .note(text)
                .build();
        repository.save(entity);
        log.info("Saved new note for userId: {}, date: {}", chatID, date);
    }
}

