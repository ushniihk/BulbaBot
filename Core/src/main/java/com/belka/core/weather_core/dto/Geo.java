package com.belka.core.weather_core.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * DTO for extracting user's geolocation from external API.
 */
@Data
public class Geo {
    /**
     * User's IP address.
     */
    @JsonProperty("query")
    @NotNull
    private String ip;
    /**
     * The name of the country whose information we received.
     */
    private String country;
    private String countryCode;
    /**
     * The name of the city whose information we received.
     */
    private String city;
    /**
     * the latitude of the city whose information we received.
     */
    private Double lat;
    /**
     * the longitude of the city whose information we received.
     */
    private Double lon;

}
