package com.belka.weather.service.geo;

/**
 * Service interface for retrieving the user's city based on their IP address.
 */

public interface GeoFromIPService {
    /**
     * Retrieves the city name based on the user's IP address.
     *
     * @return the name of the city
     */
    String getCityName();
}
