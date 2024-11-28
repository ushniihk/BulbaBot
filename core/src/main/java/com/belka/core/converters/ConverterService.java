package com.belka.core.converters;

/**
 * Service that provides a converter
 */
public interface ConverterService {

    /**
     * Converts the value to the passed type
     * @param to output type
     * @param value input value
     * @return value converted to the output type
     * @param <T> output type
     */
    <T> T convertTo(Class<T> to, Object value);
}
