package com.belka.weather.entities;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Entity for storing weather history.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "weather_history")
@Entity
public class WeatherHistoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    /**
     * Temperature in Celsius.
     */
    @NotNull
    private Integer temp;
    /**
     * Date in the format [year, month, day, hour, minute, second, nano].
     */
    @NotNull
    private LocalDateTime date;
    /**
     * City name.
     */
    @NotNull
    private String city;
}
