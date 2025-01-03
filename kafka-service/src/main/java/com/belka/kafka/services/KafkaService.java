package com.belka.kafka.services;

import com.belka.kafka.producers.KafkaProducer;
import com.belka.weather_core.models.WeatherHistory;
import com.belka.weather_core.models.weather.WeatherNow;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Collection;

@Service
@Slf4j
public class KafkaService {
    private final static String CRON_EVERY_MINUTE = "1 * * * * *";
    private final RestTemplate restTemplate;
    private final KafkaProducer producer;
    private final Collection<String> cities;
    @Value("${weather.link}")
    private String link;

    @Autowired
    public KafkaService(RestTemplate restTemplate, KafkaProducer producer,
                        @Value("${cities.list:}#{T(java.util.Collections).emptyList()}") Collection<String> cities) {
        this.restTemplate = restTemplate;
        this.producer = producer;
        this.cities = cities;
    }

    private String getWeatherLink(String city) {
        return String.format(link, city);
    }

    private WeatherNow getWeather(String city) {
        WeatherNow weatherNow = restTemplate.getForObject(getWeatherLink(city), WeatherNow.class);
        if (weatherNow == null || weatherNow.getWeatherInfo() == null) {
            throw new RuntimeException("couldn't get weather data from remote server");
        }
        return weatherNow;
    }

    @Scheduled(cron = CRON_EVERY_MINUTE)
    private void saveWeatherEveryMinute() {
        Flux.fromIterable(cities)  // Convert cities to a Flux
                .flatMap(this::saveWeatherForCity)  // Process each city asynchronously
                .subscribe();  // Trigger the reactive flow
    }

    private Mono<Void> saveWeatherForCity(String city) {
        return Mono.fromCallable(() -> {
                    WeatherNow weatherNow = getWeather(city);
                    WeatherHistory weatherHistory = WeatherHistory.builder()
                            .temp(weatherNow.getWeatherInfo().getTemp())
                            .city(city)
                            .date(LocalDateTime.now())
                            .build();
                    producer.sendMessage(weatherHistory);
                    log.info("Weather data for city {} saved", city);
                    return true;
                }).doOnError(e -> log.error("Error saving weather data for city {}: {}", city, e.getMessage()))
                .then();  // Return an empty Mono when finished
    }
}
