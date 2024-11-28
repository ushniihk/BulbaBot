package com.belka.stats.services;

import com.belka.core.converters.ConverterService;
import com.belka.stats.models.Stats;
import com.belka.stats.entities.StatsEntity;
import com.belka.stats.repositories.StatsRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class StatsServiceImpl implements StatsService {
    private final StatsRepository statsRepository;
    private final ConverterService converterService;

    @Override
    public void save(Stats dto) {
        statsRepository.save(converterService.convertTo(StatsEntity.class, dto));
    }

    public long getTotalRequests(){
        return statsRepository.count();
    }

    @Override
    public long getTotalRequestsByUser(Long chatId) {
        return statsRepository.countByUserId(chatId);
    }

    @Override
    public long getTotalRequestsByCode(String code) {
        return statsRepository.countByHandlerCode(code);
    }

    @Override
    public String getMostPopularRequest() {
        return statsRepository.getMostPopularRequest();
    }

    @Override
    public String getMostPopularRequestByUser(Long chatId) {
        return statsRepository.getMostPopularRequestByUser(chatId);
    }

}
