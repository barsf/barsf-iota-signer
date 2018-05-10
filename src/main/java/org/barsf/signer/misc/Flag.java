package org.barsf.signer.misc;

public enum Flag {

    RESET(0),
    NEXT(1),
    LAST(2);

    private final int flagCode;

    Flag(int flagCode) {
        this.flagCode = flagCode;
    }

    public int opCode() {
        return flagCode;
    }

}
