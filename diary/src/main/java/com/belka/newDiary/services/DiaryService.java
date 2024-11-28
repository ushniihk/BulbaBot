package com.belka.newDiary.services;

import com.belka.newDiary.entities.DiaryEntity;

import java.time.LocalDate;

/**
 * Service for interaction with {@link DiaryEntity}
 */
public interface DiaryService {
    /**
     * Adds a note to the diary.
     *
     * @param chatID user's id
     * @param text   the text that the user sent to the diary
     */
    void addNote(Long chatID, String text);

    /**
     * Retrieves a note for a specific date and user.
     *
     * @param date   the date of the note
     * @param chatID user's id
     * @return an Optional containing the note if it exists, otherwise an empty Optional
     */
    String getNote(LocalDate date, Long chatID);

    /**
     * Checks if a note exists on a specific day.
     *
     * @param userId user's id
     * @param date   the date to check
     * @return true if a note exists, false otherwise
     */
    boolean existsByUserIdAndDate(Long userId, LocalDate date);
}
