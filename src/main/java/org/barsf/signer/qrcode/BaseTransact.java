package org.barsf.signer.qrcode;

public abstract class BaseTransact {

    public static final int COMMAND_OFFSET = 0;
    public static final int COMMAND_LENGTH = 1;

    private Command command;

    protected BaseTransact(Command command) {
        this.command = command;
    }

    public Command getCommand() {
        return command;
    }

    public abstract String toQrCode();

    public abstract void parseFrom(String qrCode);

}
