package com.belka.weather.kafka;

import com.belka.weather.dto.WeatherHistoryDto;
import com.belka.weather.services.weather.WeatherService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;

import java.util.ArrayList;
import java.util.Collection;

//@Component
@Slf4j
@AllArgsConstructor
public class WeatherConsumer {
    private final WeatherService service;
    private final ObjectMapper objectMapper;
    @Value("${cities.list:}#{T(java.util.Collections).emptyList()}")
    private Collection<String> cities;
    private final Collection<WeatherHistoryDto> inputs = new ArrayList<>();

    @KafkaListener(topics = "${spring.kafka.consumer.topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(String input) {
        try {
            inputs.add(convertToWeatherHistory(input));
            log.info(String.format("Message received -> %s", input));
            if (inputs.size() > cities.size()) {
                service.saveBatch(inputs);
                inputs.clear();
            }
        } catch (Exception e) {
            log.error("Error processing message: {}", input, e);
        }
    }

    private WeatherHistoryDto convertToWeatherHistory(String weather) {
        try {
            return objectMapper.readValue(weather, WeatherHistoryDto.class);
        } catch (JsonProcessingException e) {
            log.error("Error converting weather to WeatherHistoryDto: {}", weather, e);
            throw new RuntimeException("Failed to convert weather to WeatherHistoryDto", e);
        }
    }
}
