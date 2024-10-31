package com.belka.weather.service.weather;

import com.belka.weather.dto.WeatherHistoryDto;

import java.util.Collection;

/**
 * Service interface for retrieving and saving weather information.
 */
public interface WeatherService {
    /**
     * Retrieves the weather response for the user's city.
     *
     * @param city the user's city
     * @return the weather response as a String
     */
    String getWeatherResponse(String city);


    /**
     * Finds the user's city.
     *
     * @return the name of the city
     */
    String findCity();


    /**
     * Saves a collection of {@link WeatherHistoryDto} to the database.
     *
     * @param weathers the collection of {@link WeatherHistoryDto} to be saved
     */
    void saveBatch(Collection<WeatherHistoryDto> weathers);
}
