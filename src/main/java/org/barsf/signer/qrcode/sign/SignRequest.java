package org.barsf.signer.qrcode.sign;

import org.barsf.signer.qrcode.BaseTransact;
import org.barsf.signer.qrcode.Command;

import java.math.BigInteger;
import java.text.NumberFormat;

public class SignRequest extends BaseTransact {

    private static final Command COMMAND = Command.SIGN_TRANSACTIONS;

    private static final int SEED_INDEX_OFFSET = COMMAND_OFFSET + COMMAND_LENGTH;
    private static final int SEED_INDEX_LENGTH = 2;
    private static final int ADDRESS_INDEX_OFFSET = SEED_INDEX_OFFSET + SEED_INDEX_LENGTH;
    private static final int ADDRESS_INDEX_LENGTH = 8;
    private static final int SECURITY_OFFSET = ADDRESS_INDEX_OFFSET + ADDRESS_INDEX_LENGTH;
    private static final int SECURITY_LENGTH = 1;
    private static final int CONTENT_OFFSET = SECURITY_OFFSET + SECURITY_LENGTH;

    private static final NumberFormat SEED_INDEX_FORMAT = NumberFormat.getInstance();
    private static final NumberFormat ADDRESS_INDEX_FORMAT = NumberFormat.getInstance();
    private static final NumberFormat SECURITY_FORMAT = NumberFormat.getInstance();

    static {
        SEED_INDEX_FORMAT.setMinimumIntegerDigits(SEED_INDEX_LENGTH);
        SEED_INDEX_FORMAT.setMaximumIntegerDigits(SEED_INDEX_LENGTH);
        SEED_INDEX_FORMAT.setGroupingUsed(false);

        ADDRESS_INDEX_FORMAT.setMinimumIntegerDigits(ADDRESS_INDEX_LENGTH);
        ADDRESS_INDEX_FORMAT.setMaximumIntegerDigits(ADDRESS_INDEX_LENGTH);
        ADDRESS_INDEX_FORMAT.setGroupingUsed(false);

        SECURITY_FORMAT.setMinimumIntegerDigits(SECURITY_LENGTH);
        SECURITY_FORMAT.setMaximumIntegerDigits(SECURITY_LENGTH);
        SECURITY_FORMAT.setGroupingUsed(false);
    }

    private int seedIndex;
    private int addressIndex;
    private int security;
    private BigInteger content;

    public SignRequest() {
        super(COMMAND);
    }

    public int getSeedIndex() {
        return seedIndex;
    }

    public void setSeedIndex(int seedIndex) {
        this.seedIndex = seedIndex;
    }

    public int getAddressIndex() {
        return addressIndex;
    }

    public void setAddressIndex(int addressIndex) {
        this.addressIndex = addressIndex;
    }

    public int getSecurity() {
        return security;
    }

    public void setSecurity(int security) {
        this.security = security;
    }

    public BigInteger getContent() {
        return content;
    }

    public void setContent(BigInteger content) {
        this.content = content;
    }

    @Override
    public String toQrCode() {
        return COMMAND.commandCode() + SEED_INDEX_FORMAT.format(seedIndex) + ADDRESS_INDEX_FORMAT.format(addressIndex)
                + SECURITY_FORMAT.format(security) + content.toString();
    }

    @Override
    public void parseFrom(String qrCode) {
        Command command = Command.values()[Integer.parseInt(qrCode.substring(COMMAND_OFFSET, COMMAND_OFFSET + COMMAND_LENGTH))];
        if (command != COMMAND) {
            throw new RuntimeException("ops, this is not a SignRequest, actually the command is " + command);
        }
        seedIndex = Integer.parseInt(qrCode.substring(SEED_INDEX_OFFSET, SEED_INDEX_OFFSET + SEED_INDEX_LENGTH));
        addressIndex = Integer.parseInt(qrCode.substring(ADDRESS_INDEX_OFFSET, ADDRESS_INDEX_OFFSET + ADDRESS_INDEX_LENGTH));
        security = Integer.parseInt(qrCode.substring(SECURITY_OFFSET, SECURITY_OFFSET + SECURITY_LENGTH));
        content = new BigInteger(qrCode.substring(CONTENT_OFFSET));
    }
}
