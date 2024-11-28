package com.belka.weather.services.weather;

import com.belka.core.converters.ConverterService;
import com.belka.weather.dto.WeatherHistoryDto;
import com.belka.weather.entities.WeatherHistoryEntity;
import com.belka.weather.repositories.WeatherRepository;
import com.belka.weather.services.geo.GeoFromIPService;
import com.belka.weather_core.models.weather.WeatherInfo;
import com.belka.weather_core.models.weather.WeatherNow;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collection;

@Service
@Slf4j
@Data
public class WeatherServiceImpl implements WeatherService {

    @Value("${weather.key}")
    private String key;
    @Value("${weather.link}")
    private String link;
    private final RestTemplate restTemplate;
    private final GeoFromIPService geoFromIPService;
    private WeatherRepository repository;
    private ConverterService converterService;

    @Autowired
    public void setConverterService(ConverterService converterService) {
        this.converterService = converterService;
    }

    @Autowired
    public void setRepository(WeatherRepository repository) {
        this.repository = repository;
    }

    public String getWeatherResponse(String city) {
        WeatherInfo weatherInfo = getWeather(city).getWeatherInfo();
        String text = city + "\n" + "temp is " + weatherInfo.getTemp() + ", filling like " + weatherInfo.getFeelsLike();
        if (weatherInfo.getTemp() < 0) {
            return "⛄" + text + "⛄";
        }
        return text;
    }

    private WeatherNow getWeather(String city) {
        WeatherNow weatherNow = restTemplate.getForObject(getWeatherLink(city), WeatherNow.class);
        if (weatherNow == null || weatherNow.getWeatherInfo() == null) {
            log.error("Couldn't get weather data for city: {}", city);
            throw new RuntimeException("Couldn't get weather data from remote server");
        }
        return weatherNow;
    }

    private String getWeatherLink(String city) {
        return String.format(link, city);
    }

    public String findCity() {
        return geoFromIPService.getCityName();
    }

    @Override
    public void saveBatch(Collection<WeatherHistoryDto> weathers) {
        Collection<WeatherHistoryEntity> entities = new ArrayList<>();
        for (WeatherHistoryDto weather : weathers) {
            WeatherHistoryEntity entity = converterService.convertTo(WeatherHistoryEntity.class, weather);
            entities.add(entity);
            log.info("Message received -> {}", entity);
        }
        repository.batchSave(entities);
        log.info("Messages saved");
    }


}
