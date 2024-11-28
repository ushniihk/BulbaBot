package com.belka.core.previous_step.interfaces;


/**
 * Information about user's previous action
 */
public interface PreviousStep {
    Long getUserId();

    /**
     * Previous handler's name that user has been visited
     */
    String getPreviousStep();

    /**
     * Next handler's name that user has to visit
     */
    String getNextStep();

    /**
     * Data that received from previous step
     */
    String getData();
}
