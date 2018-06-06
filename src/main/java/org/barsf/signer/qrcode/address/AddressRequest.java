package org.barsf.signer.qrcode.address;

import org.barsf.signer.qrcode.BaseTransact;
import org.barsf.signer.qrcode.Command;

import java.text.NumberFormat;

public class AddressRequest extends BaseTransact {

    private static final Command COMMAND = Command.ADDRESS;

    private static final int SEED_INDEX_OFFSET = COMMAND_OFFSET + COMMAND_LENGTH;
    private static final int SEED_INDEX_LENGTH = 2;
    private static final int FROM_INDEX_OFFSET = SEED_INDEX_OFFSET + SEED_INDEX_LENGTH;
    private static final int FROM_INDEX_LENGTH = 10;
    private static final int TO_INDEX_OFFSET = FROM_INDEX_OFFSET + FROM_INDEX_LENGTH;
    private static final int TO_INDEX_LENGTH = 10;
    private static final int SECURITY_OFFSET = TO_INDEX_OFFSET + TO_INDEX_LENGTH;
    private static final int SECURITY_LENGTH = 1;

    private static final NumberFormat SEED_INDEX_FORMAT = NumberFormat.getInstance();
    private static final NumberFormat FROM_TO_INDEX_FORMAT = NumberFormat.getInstance();

    static {
        SEED_INDEX_FORMAT.setMinimumIntegerDigits(SEED_INDEX_LENGTH);
        SEED_INDEX_FORMAT.setMaximumIntegerDigits(SEED_INDEX_LENGTH);
        SEED_INDEX_FORMAT.setGroupingUsed(false);

        FROM_TO_INDEX_FORMAT.setMinimumIntegerDigits(FROM_INDEX_LENGTH);
        FROM_TO_INDEX_FORMAT.setMaximumIntegerDigits(FROM_INDEX_LENGTH);
        FROM_TO_INDEX_FORMAT.setGroupingUsed(false);
    }

    private int seedIndex;
    private int fromIndex;
    private int toIndex;
    private int security;

    public AddressRequest() {
        super(COMMAND);
    }

    public int getSeedIndex() {
        return seedIndex;
    }

    public void setSeedIndex(int seedIndex) {
        this.seedIndex = seedIndex;
    }

    public int getFromIndex() {
        return fromIndex;
    }

    public void setFromIndex(int fromIndex) {
        this.fromIndex = fromIndex;
    }

    public int getToIndex() {
        return toIndex;
    }

    public void setToIndex(int toIndex) {
        this.toIndex = toIndex;
    }

    public int getSecurity() {
        return security;
    }

    public void setSecurity(int security) {
        if (security < 1 || security > 3) {
            throw new RuntimeException("security should be a integer which value >= 1 and <= 3");
        } else {
            this.security = security;
        }
    }

    @Override
    public String toQrCode() {
        return COMMAND.commandCode() + SEED_INDEX_FORMAT.format(seedIndex) + FROM_TO_INDEX_FORMAT.format(fromIndex)
                + FROM_TO_INDEX_FORMAT.format(toIndex) + security;
    }

    @Override
    public void parseFrom(String qrCode) {
        Command command = Command.values()[Integer.parseInt(qrCode.substring(COMMAND_OFFSET, COMMAND_OFFSET + COMMAND_LENGTH))];
        if (command != COMMAND) {
            throw new RuntimeException("ops, this is not a AddressRequest, actually the command is " + command);
        }
        seedIndex = Integer.parseInt(qrCode.substring(SEED_INDEX_OFFSET, SEED_INDEX_OFFSET + SEED_INDEX_LENGTH));
        fromIndex = Integer.parseInt(qrCode.substring(FROM_INDEX_OFFSET, FROM_INDEX_OFFSET + FROM_INDEX_LENGTH));
        toIndex = Integer.parseInt(qrCode.substring(TO_INDEX_OFFSET, TO_INDEX_OFFSET + TO_INDEX_LENGTH));
        security = Integer.parseInt(qrCode.substring(SECURITY_OFFSET));
    }
}
