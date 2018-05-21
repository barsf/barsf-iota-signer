package org.barsf.signer.exception;

public class CommunicationInterruptedException extends Exception {

    /**
     * Constructs a {@code CommunicationInterruptedException} with no specified detail
     * message.
     */
    public CommunicationInterruptedException() {
    }

    /**
     * Constructs a {@code CommunicationInterruptedException} with the specified detail
     * message.
     *
     * @param message the detail message
     */
    public CommunicationInterruptedException(String message) {
        super(message);
    }

}
