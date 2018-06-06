package org.barsf.signer.qrcode.sign;

import org.barsf.signer.qrcode.BaseTransact;
import org.barsf.signer.qrcode.Command;

import java.math.BigInteger;

public class SignResponse extends BaseTransact {

    private static final Command COMMAND = Command.SIGN_TRANSACTIONS;

    private BigInteger signature;

    public SignResponse() {
        super(COMMAND);
    }

    public BigInteger getSignature() {
        return signature;
    }

    public void setSignature(BigInteger signature) {
        this.signature = signature;
    }

    @Override
    public String toQrCode() {
        return signature.toString();
    }

    @Override
    public void parseFrom(String qrCode) {
        signature = new BigInteger(qrCode);
    }
}
