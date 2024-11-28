package com.belka.core.converters;

/**
 * converter
 *
 * @param <I> input type
 * @param <O> output type
 */
public interface BelkaConverter<I, O> {
    /**
     * converts input object to output type
     *
     * @param input input object
     * @return object converted to output type
     */
    default O convertObject(Object input) {
        if (input == null) {
            return null;
        }
        if (getInputType().isInstance(input)) {
            return convert(getInputType().cast(input));
        } else {
            throw new IllegalArgumentException("Conversion failed. Expected input type: "
                    + getInputType().getTypeName() + ", but received: "
                    + input.getClass().getTypeName());
        }
    }

    /**
     * converts input value to output type
     *
     * @param value input value
     * @return value converted to output type
     */

    O convert(I value);

    /**
     * checks if value is null
     *
     * @param value value to check
     */
    default void checkValue(I value) {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }
    }

    Class<O> getOutputType();

    Class<I> getInputType();
}
