package com.belka.stats.services;

import com.belka.core.models.BelkaEvent;
import com.belka.stats.models.Stats;

/**
 * collects and processes stats
 */
public interface StatsService {
    /**
     * save stats
     *
     * @param dto {@link Stats}
     */
    void save(Stats dto);

    /**
     * get number of requests
     */
    Long getTotalRequests();

    /**
     * get number of requests by user's id
     *
     * @param chatId {@link BelkaEvent 's chatId}
     */
    Long getTotalRequestsByUser(Long chatId);

    /**
     * get number of requests by code
     *
     * @param code {@link com.belka.core.handlers.BelkaHandler's code}
     */
    Long getTotalRequestsByCode(String code);

    /**
     * get {@link com.belka.core.handlers.BelkaHandler's code} with the maximum number of requests
     */
    String getMostPopularRequest();

    /**
     * get {@link com.belka.core.handlers.BelkaHandler's code} with the maximum number of requests from user
     *
     * @param chatId {@link BelkaEvent 's chatId}
     */
    String getMostPopularRequestByUser(Long chatId);
    Long countAllUsers();

}
