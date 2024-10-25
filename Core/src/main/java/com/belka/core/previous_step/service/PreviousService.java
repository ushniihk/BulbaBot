package com.belka.core.previous_step.service;

import com.belka.core.previous_step.dto.PreviousStepDto;
import com.belka.core.previous_step.interfaces.PreviousStep;

/**
 * Service for tracking the user's previous steps.
 */
public interface PreviousService {
    /**
     * Saves the {@link PreviousStep previous step}
     *
     * @param dto the DTO of the {@link PreviousStep previous step}
     */
    void save(PreviousStepDto dto);

    /**
     * Gets the {@link PreviousStep previous step}
     *
     * @param chatId the user ID
     * @return code of the previous step
     */
    String getPreviousStep(Long chatId);

    /**
     * Gets the next step code by user ID.
     *
     * @param chatId user's id
     * @return code of the next step
     */
    String getNextStep(Long chatId);

    /**
     * get get {@link PreviousStep previous step} date
     *
     * @param chatId user's id
     * @return data from the previous step
     */
    String getData(Long chatId);
}
