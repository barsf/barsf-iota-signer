package org.barsf.signer.exception;

public class PeerNoResponseException extends Exception {

    /**
     * Constructs a {@code PeerNoResponseException} with no specified detail
     * message.
     */
    public PeerNoResponseException() {
    }

    /**
     * Constructs a {@code PeerNoResponseException} with the specified detail
     * message.
     *
     * @param message the detail message
     */
    public PeerNoResponseException(String message) {
        super(message);
    }

}
