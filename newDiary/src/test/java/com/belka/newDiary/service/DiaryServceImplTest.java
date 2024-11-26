package com.belka.newDiary.service;

import com.belka.newDiary.entity.DiaryEntity;
import com.belka.newDiary.repository.DiaryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DiaryServiceImplTest {

    @Mock
    private DiaryRepository repository;

    @InjectMocks
    private DiaryServiceImpl diaryServiceImpl;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void addNote_createsNewNoteWhenNoneExists() {
        Long chatID = 1L;
        String text = "New note";
        LocalDate today = LocalDate.now();

        when(repository.getByDateAndUserId(today, chatID)).thenReturn(Optional.empty());

        diaryServiceImpl.addNote(chatID, text);

        verify(repository, times(1)).save(any(DiaryEntity.class));
    }

    @Test
    void addNote_updatesExistingNoteWhenExists() {
        Long chatID = 1L;
        String text = "Updated note";
        LocalDate today = LocalDate.now();
        DiaryEntity existingNote = new DiaryEntity();
        existingNote.setNote("Existing note");

        when(repository.getByDateAndUserId(today, chatID)).thenReturn(Optional.of(existingNote));

        diaryServiceImpl.addNote(chatID, text);

        verify(repository, times(1)).save(existingNote);
        assertEquals("Existing note\nUpdated note", existingNote.getNote());
    }

    @Test
    void getNote_returnsNoteWhenExists() {
        Long chatID = 1L;
        LocalDate date = LocalDate.now();
        String note = "Existing note";

        when(repository.findNoteByUserIdAndDate(chatID, date)).thenReturn(Optional.of(note));

        String result = diaryServiceImpl.getNote(date, chatID);

        assertEquals(note, result);
    }

    @Test
    void getNote_returnsNoNoteFoundWhenNoneExists() {
        Long chatID = 1L;
        LocalDate date = LocalDate.now();

        when(repository.findNoteByUserIdAndDate(chatID, date)).thenReturn(Optional.empty());

        String result = diaryServiceImpl.getNote(date, chatID);

        assertEquals("No note found for this date", result);
    }

    @Test
    void existsByUserIdAndDate_returnsTrueWhenExists() {
        Long userId = 1L;
        LocalDate date = LocalDate.now();

        when(repository.existsByUserIdAndDate(userId, date)).thenReturn(true);

        boolean result = diaryServiceImpl.existsByUserIdAndDate(userId, date);

        assertTrue(result);
    }

    @Test
    void existsByUserIdAndDate_returnsFalseWhenNotExists() {
        Long userId = 1L;
        LocalDate date = LocalDate.now();

        when(repository.existsByUserIdAndDate(userId, date)).thenReturn(false);

        boolean result = diaryServiceImpl.existsByUserIdAndDate(userId, date);

        assertFalse(result);
    }
}