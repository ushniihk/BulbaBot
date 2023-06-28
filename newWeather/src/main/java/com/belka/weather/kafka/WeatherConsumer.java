package com.belka.weather.kafka;

import com.belka.weather.json.JsonWeatherHistory;
import com.belka.weather.service.weather.WeatherService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;

@Component
@Slf4j
public class WeatherConsumer {
    private final WeatherService service;
    private final String TOPIC = "weather";
    private final String GROUP_ID = "myGroup";
    private final Collection<JsonWeatherHistory> inputs = new ArrayList<>();

    @Autowired
    public WeatherConsumer(WeatherService service) {
        this.service = service;
    }

    @KafkaListener(topics = TOPIC, groupId = GROUP_ID)
    public void consume(String input) {
        inputs.add(convertToWeatherHistory(input));
        log.info(String.format("Message received -> %s", input));
        if (inputs.size() > 5) {
            service.saveBatch(inputs);
            inputs.clear();
        }
    }

    private JsonWeatherHistory convertToWeatherHistory(String weather) {
        ObjectMapper mapper = new ObjectMapper();
        JsonWeatherHistory jsonWeatherHistory;
        try {
            jsonWeatherHistory = mapper.readValue(weather, JsonWeatherHistory.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return jsonWeatherHistory;

    }
}