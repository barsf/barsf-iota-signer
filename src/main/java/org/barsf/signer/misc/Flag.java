package org.barsf.signer.misc;

public enum Flag {

    NA(0),
    RESET(1),
    NEXT(2),
    LAST(3);

    private final int flagCode;

    Flag(int flagCode) {
        this.flagCode = flagCode;
    }

    public int opCode() {
        return flagCode;
    }

}
