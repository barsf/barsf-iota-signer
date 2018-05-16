package org.barsf.signer.exception;

public class ReadTimeoutException extends Exception {

    /**
     * Constructs a {@code ReadTimeoutException} with no specified detail
     * message.
     */
    public ReadTimeoutException() {
    }

    /**
     * Constructs a {@code ReadTimeoutException} with the specified detail
     * message.
     *
     * @param message the detail message
     */
    public ReadTimeoutException(String message) {
        super(message);
    }

}
