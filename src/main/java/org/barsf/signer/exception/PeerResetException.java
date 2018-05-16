package org.barsf.signer.exception;

public class PeerResetException extends Exception {

    /**
     * Constructs a {@code PeerResetException} with no specified detail
     * message.
     */
    public PeerResetException() {
    }

    /**
     * Constructs a {@code PeerResetException} with the specified detail
     * message.
     *
     * @param message the detail message
     */
    public PeerResetException(String message) {
        super(message);
    }

}
