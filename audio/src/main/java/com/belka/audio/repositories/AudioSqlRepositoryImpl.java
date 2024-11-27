package com.belka.audio.repositories;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@AllArgsConstructor
@Repository
@Slf4j
public class AudioSqlRepositoryImpl implements AudioSqlRepository {
    private final JdbcTemplate jdbcTemplate;
    /**
     * This query will select all subscribers and all new audio recordings created for the current day
     * and add them to the not_listed table
     */
    private final static String SQL_QUERY_FILL = """
            INSERT INTO not_listened (subscriber, audio_id)
            SELECT s.subscriber, a.id
            FROM subscriptions s
            JOIN audio a
            ON s.producer = a.user_id
            WHERE a.date = CURRENT_DATE - INTERVAL '1 DAY'
            ON CONFLICT DO NOTHING
            """;

    private final static String SQL_QUERY_DELETE = "DELETE FROM not_listened WHERE subscriber = ? AND audio_id = ?";

    @Override
    public void fillNotListened() {
        try {
            jdbcTemplate.execute(SQL_QUERY_FILL);
        } catch (DataAccessException e) {
            log.error("Error executing fillNotListened query: {}", e.getMessage(), e);
        }
    }

    @Override
    public void deleteFromNotListened(Long userId, String fileId) {
        try {
            jdbcTemplate.update(SQL_QUERY_DELETE, userId, fileId);
        } catch (DataAccessException e) {
            log.error("Error executing deleteFromNotListened query: {}", e.getMessage(), e);
        }
    }
}
