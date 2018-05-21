package org.barsf.signer.exception;

public class QrCodeNotFundException extends Exception {

    /**
     * Constructs a {@code QrCodeNotFundException} with no specified detail
     * message.
     */
    public QrCodeNotFundException() {
    }

    /**
     * Constructs a {@code QrCodeNotFundException} with the specified detail
     * message.
     *
     * @param message the detail message
     */
    public QrCodeNotFundException(String message) {
        super(message);
    }

}
