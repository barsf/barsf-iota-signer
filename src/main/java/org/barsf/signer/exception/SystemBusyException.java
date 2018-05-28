package org.barsf.signer.exception;

public class SystemBusyException extends Exception {

    /**
     * Constructs a {@code SystemBusyException} with no specified detail
     * message.
     */
    public SystemBusyException() {
    }

    /**
     * Constructs a {@code SystemBusyException} with the specified detail
     * message.
     *
     * @param message the detail message
     */
    public SystemBusyException(String message) {
        super(message);
    }

}
