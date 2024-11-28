package com.belka.weather.services.getIp;

import com.belka.weather_core.dto.IP;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Data
@Service
public class GetIPServiceImpl implements GetIPService {
    private final RestTemplate restTemplate;
    @Value("${weather.ip.link}")
    private String link;

    @Override
    public String getIP() {
        IP ip = restTemplate.getForObject(link, IP.class);
        if (ip == null || ip.getIp() == null) {
            throw new RuntimeException("couldn't read this IP");
        }
        return ip.getIp();
    }
}
