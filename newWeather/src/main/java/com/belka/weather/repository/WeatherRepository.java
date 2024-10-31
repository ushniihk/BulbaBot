package com.belka.weather.repository;

import com.belka.weather.entity.WeatherHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for {@link WeatherHistoryEntity WeatherHistory}.
 */
public interface WeatherRepository extends JpaRepository<WeatherHistoryEntity, Long>, WeatherSQLRepository {
}
