package com.belka.weather.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Data Transfer Object for Weather History.
 */
@Data
public class WeatherHistoryDto {
    private Long id;
    /**
     * Temperature in Celsius.
     */
    @NotNull
    private Integer temp;
    /**
     * Date in the format [year, month, day, hour, minute, second, nano].
     */
    @Size(min = 7, max = 7, message = "Date array must have exactly 7 elements")
    private int[] date;
    /**
     * City name.
     */
    @NotNull
    private String city;
}
