package org.barsf.signer.exception;

public class IncompatibleVersionException extends Exception {

    /**
     * Constructs a {@code TimeoutException} with no specified detail
     * message.
     */
    public IncompatibleVersionException() {
    }

    /**
     * Constructs a {@code TimeoutException} with the specified detail
     * message.
     *
     * @param message the detail message
     */
    public IncompatibleVersionException(String message) {
        super(message);
    }

}
