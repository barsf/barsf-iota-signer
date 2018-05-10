package org.barsf.signer.exception;

public class CommunicationInterruptedException extends Exception {

    /**
     * Constructs a {@code TimeoutException} with no specified detail
     * message.
     */
    public CommunicationInterruptedException() {
    }

    /**
     * Constructs a {@code TimeoutException} with the specified detail
     * message.
     *
     * @param message the detail message
     */
    public CommunicationInterruptedException(String message) {
        super(message);
    }

}
