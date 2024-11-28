package com.belka.weather.services.geo;

import com.belka.weather.services.getIp.GetIPService;
import com.belka.weather_core.dto.Geo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Data
@Service
@Slf4j
public class GeoFromIPServiceImpl implements GeoFromIPService {

    @Value("${weather.geo-ip.link}")
    private String link;
    private final GetIPService getIPService;
    private final RestTemplate restTemplate;

    @Override
    public String getCityName() {
        String fullLink = getLink();
        try {
            Geo geo = restTemplate.getForObject(fullLink, Geo.class);
            if (geo == null || geo.getCity() == null) {
                log.error("Failed to retrieve city from Geo object: {}", geo);
                throw new RuntimeException("Couldn't read the city");
            }
            return geo.getCity();
        } catch (Exception e) {
            log.error("Error fetching city name from IP: {}", e.getMessage(), e);
            throw new RuntimeException("Error fetching city name", e);
        }
    }

    private String getLink() {
        String ip = getIPService.getIP();
        if (ip == null || ip.isEmpty()) {
            log.error("IP address is null or empty");
            throw new RuntimeException("IP address is null or empty");
        }
        return link + ip;
    }
}
