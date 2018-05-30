package org.barsf.signer.qrcode;

public enum Command {

    ADDRESS(0),
    SIGN_TRANSACTIONS(1),
    SIGN_MILESTONE(2);

    private final int commandCode;

    Command(int commandCode) {
        this.commandCode = commandCode;
    }

    public int commandCode() {
        return commandCode;
    }

}
