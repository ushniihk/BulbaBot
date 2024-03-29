package com.belka.core.weather_core.dto;

import lombok.Data;

/**
 * DTO for extract user's IP with external API
 */
@Data
public class IP {
    /**
     * user's IP
     */
    private String ip;
}
