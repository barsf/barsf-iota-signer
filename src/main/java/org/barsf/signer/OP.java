package org.barsf.signer;

public enum OP {

    RESET(0), NEXT_FRAGMENT(1), NEW_TRANS_SIGN(2), NEW_MS_SIGN(3), ERROR(7), RESPONSE(8), SLEEP(9);

    private final int opCode;

    OP(int opCode) {
        this.opCode = opCode;
    }

    public int opCode() {
        return opCode;
    }

}
