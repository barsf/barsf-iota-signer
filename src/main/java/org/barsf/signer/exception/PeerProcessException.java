package org.barsf.signer.exception;

public class PeerProcessException extends Exception {

    /**
     * Constructs a {@code PeerProcessException} with no specified detail
     * message.
     */
    public PeerProcessException() {
    }

    /**
     * Constructs a {@code PeerProcessException} with the specified detail
     * message.
     *
     * @param message the detail message
     */
    public PeerProcessException(String message) {
        super(message);
    }

}
