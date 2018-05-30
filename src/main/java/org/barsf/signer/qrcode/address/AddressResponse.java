package org.barsf.signer.qrcode.address;

import org.apache.commons.lang3.StringUtils;
import org.barsf.iota.lib.utils.Unsigned;
import org.barsf.signer.qrcode.BaseTransact;
import org.barsf.signer.qrcode.Command;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class AddressResponse extends BaseTransact {

    public static final Command COMMAND = Command.ADDRESS;

    public static final int ADDRESS_LENGTH = Unsigned.u27To10(Unsigned.trytes(StringUtils.repeat('M', 81))).toString().length();

    private List<BigInteger> addresses;

    public AddressResponse() {
        super(COMMAND);
    }

    public List<BigInteger> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<BigInteger> addresses) {
        this.addresses = addresses;
    }

    @Override
    public String toQrCode() {
        StringBuilder sb = new StringBuilder();
        addresses.forEach(address -> sb.append(StringUtils.leftPad(address.toString(), ADDRESS_LENGTH, '0')));
        return sb.toString();
    }

    @Override
    public void parseFrom(String qrCode) {
        addresses = new ArrayList<>();
        for (int i = 0; i < qrCode.length() / ADDRESS_LENGTH; i++) {
            addresses.add(new BigInteger(qrCode.substring(i * ADDRESS_LENGTH, i * ADDRESS_LENGTH + ADDRESS_LENGTH)));
        }
    }

}
