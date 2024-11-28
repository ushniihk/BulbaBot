package com.belka.weather.repositories;

import com.belka.weather.entities.WeatherHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for {@link WeatherHistoryEntity WeatherHistory}.
 */
public interface WeatherRepository extends JpaRepository<WeatherHistoryEntity, Long>, WeatherSQLRepository {
}
