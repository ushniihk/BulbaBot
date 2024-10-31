package com.belka.weather.repository;

import com.belka.weather.entity.WeatherHistoryEntity;

import java.util.Collection;

/**
 * Custom repository interface for batch operations on {@link WeatherHistoryEntity}.
 */
public interface WeatherSQLRepository {
    /**
     * Saves a collection of {@link WeatherHistoryEntity} in a batch operation.
     *
     * @param entities the collection of entities to be saved
     */
    void batchSave(Collection<WeatherHistoryEntity> entities);
}
