package de.victorarcon.spured;

/**
 * Thrown when a requested Grosskunde entity cannot be found.
 */
public class GrosskundeException extends RuntimeException {

    private GrosskundeException(String message) {
        super(message);
    }

    /**
     * Creates an exception for a Grosskunde that could not be found by ID.
     *
     * @param id the ID that was looked up
     * @return a new {@code GrosskundeException} with a descriptive message
     */
    public static GrosskundeException notFound(Long id) {
        return new GrosskundeException("Grosskunde with id " + id + " could not be found");
    }
}
