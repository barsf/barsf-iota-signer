package org.barsf.signer.qrcode.milestone;

import org.apache.commons.lang3.StringUtils;
import org.barsf.iota.lib.utils.Unsigned;
import org.barsf.signer.qrcode.BaseTransact;
import org.barsf.signer.qrcode.Command;

import java.math.BigInteger;

public class MileStoneResponse extends BaseTransact {

    private static final Command COMMAND = Command.SIGN_MILESTONE;

    private static final int MS_SIGN_OFFSET = 0;
    private static final int MS_SIGN_LENGTH = Unsigned.u27To10(Unsigned.trytes(StringUtils.repeat('M', 2187))).toString().length();
    private static final int MS_PATH_OFFSET = MS_SIGN_OFFSET + MS_SIGN_LENGTH;

    private BigInteger sign;
    private BigInteger path;

    public MileStoneResponse() {
        super(COMMAND);
    }

    public BigInteger getSign() {
        return sign;
    }

    public void setSign(BigInteger sign) {
        this.sign = sign;
    }

    public BigInteger getPath() {
        return path;
    }

    public void setPath(BigInteger path) {
        this.path = path;
    }

    @Override
    public String toQrCode() {
        return StringUtils.leftPad(sign.toString(), MS_SIGN_LENGTH, '0') + path.toString();
    }

    @Override
    public void parseFrom(String qrCode) {
        sign = new BigInteger(qrCode.substring(MS_SIGN_OFFSET, MS_SIGN_LENGTH));
        path = new BigInteger(qrCode.substring(MS_PATH_OFFSET));
    }
}
