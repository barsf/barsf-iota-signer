package org.barsf.signer.misc;

public enum Flag {

    NA(0),
    RESET(1),
    RESETACK(2),
    NEXT(3),
    LAST(4);

    private final int flagCode;

    Flag(int flagCode) {
        this.flagCode = flagCode;
    }

    public int opCode() {
        return flagCode;
    }

}
