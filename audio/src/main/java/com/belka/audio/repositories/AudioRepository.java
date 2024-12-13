package com.belka.audio.repositories;

import com.belka.audio.entities.AudioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;


public interface AudioRepository extends JpaRepository<AudioEntity, String>, AudioSqlRepository {
    /**
     * Updates the isPublic flag of an audio entity.
     *
     * @param flag   the new value of the isPublic flag
     * @param fileId the ID of the audio entity
     */
    @Modifying
    @Transactional
    @Query("UPDATE AudioEntity a SET a.isPublic = :flag WHERE a.id = :fileId")
    void changeIsPrivateFlag(boolean flag, String fileId);

    boolean existsByDateAndUserId(LocalDate date, Long userId);

    @Query(value = "SELECT a.id FROM audio a WHERE a.date = :date AND a.user_id = :userId ORDER BY a.id ASC LIMIT 1", nativeQuery = true)
    Optional<String> getIdByDateAndUserId(LocalDate date, Long userId);
}