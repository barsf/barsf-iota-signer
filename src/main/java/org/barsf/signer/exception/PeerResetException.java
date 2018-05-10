package org.barsf.signer.exception;

public class PeerResetException extends Exception {

    /**
     * Constructs a {@code TimeoutException} with no specified detail
     * message.
     */
    public PeerResetException() {
    }

    /**
     * Constructs a {@code TimeoutException} with the specified detail
     * message.
     *
     * @param message the detail message
     */
    public PeerResetException(String message) {
        super(message);
    }

}
