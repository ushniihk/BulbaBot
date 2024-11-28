package com.belka.weather_core.models.weather;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * DTO for extracting at the moment weather information from external API.
 */
@Data
public class WeatherNow {
    /**
     * The collection of {@link Weather weather} information.
     */
    private List<Weather> weather;
    /**
     * information about the weather in the city.
     */
    @JsonProperty("main")
    private WeatherInfo weatherInfo;
}

