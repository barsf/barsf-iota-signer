package org.barsf.signer.exception;

public class FragmentTooLongException extends Exception {

    /**
     * Constructs a {@code TimeoutException} with no specified detail
     * message.
     */
    public FragmentTooLongException() {
    }

    /**
     * Constructs a {@code TimeoutException} with the specified detail
     * message.
     *
     * @param message the detail message
     */
    public FragmentTooLongException(String message) {
        super(message);
    }

}
