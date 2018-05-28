package org.barsf.signer.misc;

public enum Command {

    SIGN_TRANSACTIONS(0),
    SIGN_MILESTONE(1);

    private final int commandCode;

    Command(int commandCode) {
        this.commandCode = commandCode;
    }

    public int commandCode() {
        return commandCode;
    }

}
