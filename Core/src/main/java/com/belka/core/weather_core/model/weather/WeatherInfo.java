package com.belka.core.weather_core.model.weather;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
/**
 * DTO for extracting weather information from external API.
 */
@Data
public class WeatherInfo {
    /**
     * Temperature in the city.
     */
    private Integer temp;
    /**
     * The temperature feels like in the city.
     */
    @JsonProperty("feels_like")
    private Integer feelsLike;
}
