package org.barsf.signer.exception;

public class TimeoutException extends Exception {

    /**
     * Constructs a {@code TimeoutException} with no specified detail
     * message.
     */
    public TimeoutException() {
    }

    /**
     * Constructs a {@code TimeoutException} with the specified detail
     * message.
     *
     * @param message the detail message
     */
    public TimeoutException(String message) {
        super(message);
    }

}
