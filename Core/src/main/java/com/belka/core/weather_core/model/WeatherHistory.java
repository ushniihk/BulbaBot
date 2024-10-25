package com.belka.core.weather_core.model;

import lombok.*;

import java.time.LocalDateTime;

//todo do we need to have the weather models in the Core module?
@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class WeatherHistory {
    /**
     * Unique identifier for the weather history record.
     */
    private Long id;
    /**
     * Temperature recorded.
     */
    private Integer temp;
    /**
     * Date and time when the temperature was recorded.
     */
    private LocalDateTime date;
    /**
     * City where the temperature was recorded.
     */
    private String city;
}
