package org.barsf.signer;

import com.google.zxing.WriterException;
import org.apache.commons.lang3.StringUtils;
import org.barsf.camera.main.Camera;
import org.barsf.screen.main.Screen;
import org.barsf.signer.exception.PeerResetException;
import org.barsf.signer.exception.TimeoutException;
import org.barsf.signer.misc.Flag;
import org.barsf.signer.misc.Fragment;


public abstract class Base {

    private static final int SLEEP_INTERVAL = 250;
    private static final int TIME_OUT_TRIGGER_TO_RETRY = 5 * 1000;
    private static final int TIME_OUT_TRIGGER_TO_RESET = 15 * 1000;

    private Screen screen;
    private Camera camera;

    private Fragment previousFragSent = null;
    private Fragment previousFragRecv = null;

    private String previousQrCodeRecv = null;

    protected Base() {
        this.screen = new Screen();
        this.camera = new Camera();
    }

    private void reset() {
        this.previousFragSent = null;
        this.previousFragRecv = null;
    }

    protected String sendAndReceive(String command)
            throws TimeoutException, WriterException, PeerResetException {
        if (command != null) {
        }
        boolean needNextResponse = true;
        StringBuilder fullResponse = new StringBuilder();
        while (StringUtils.length(command) > 0 || needNextResponse) {
            Fragment fragment = null;
            if (command != null) {
                fragment = new Fragment();
                if (command.length() > Fragment.MAX_CONTENT_LENGTH) {
                    fragment.setFlag(Flag.NEXT);
                } else {
                    fragment.setFlag(Flag.LAST);
                }
                fragment.setFragmentContent(StringUtils.substring(command, 0, Fragment.MAX_CONTENT_LENGTH));
            }
            Fragment lastResponse = writeAndRead(fragment);
            switch (lastResponse.getFlag()) {
                case RESET:
                    sendResetAck();
                    throw new PeerResetException();
                case LAST:
                    needNextResponse = false;
                    fullResponse.append(lastResponse.getFragmentContent());
                    break;
                case NEXT:
                    needNextResponse = true;
                    command = "";
                    fullResponse.append(lastResponse.getFragmentContent());
                    break;
            }
            if (command != null) {
                command = StringUtils.substring(command, Fragment.MAX_CONTENT_LENGTH);
            }
        }
        return fullResponse.toString();
    }

    protected void sendResetAndGetAck()
            throws TimeoutException, WriterException {
        System.out.println("sending reset");
        reset();
        Fragment fragment = new Fragment();
        fragment.setFlag(Flag.RESET);
        writeAndRead(fragment);
    }

    private void sendResetAck() throws WriterException {
        // warning: do NOT invoke reset method
        System.out.println("remote reset");
        Fragment fragment = new Fragment();
        fragment.setFlag(Flag.LAST);
        write(fragment);
    }

    private Fragment writeAndRead(Fragment fragment)
            throws WriterException, TimeoutException {
        long firstSent = System.currentTimeMillis();
        while (true) {
            if (System.currentTimeMillis() - firstSent >= TIME_OUT_TRIGGER_TO_RESET) {
                throw new TimeoutException();
            }
            write(fragment);
            Fragment returnValue = readNext();
            if (returnValue == null) {
                continue;
            }
            if (fragment != null) {
                System.out.println(StringUtils.leftPad(fragment.getFragmentContent().length() + "", 4, '0')
                        + " : " + StringUtils.leftPad(System.currentTimeMillis() - firstSent + "", 5, '0'));
            } else {
                System.out.println(StringUtils.leftPad("0", 4, '0')
                        + " : " + StringUtils.leftPad(System.currentTimeMillis() - firstSent + "", 5, '0'));
            }
            return returnValue;
        }
    }

    private void write(Fragment fragment) throws WriterException {
        if (fragment != null) {
            fragment.newNonce();
            if (previousFragRecv != null) {
                fragment.setPreviousChecksum(previousFragRecv.getChecksum());
            }
            screen.display(fragment.toQrCode());
            previousFragSent = fragment;
        }
    }

    private Fragment readNext() {
        long beginTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - beginTime < TIME_OUT_TRIGGER_TO_RETRY) {
            String qrCode = camera.scan();
            if (StringUtils.isBlank(qrCode)
                    || qrCode.length() < Fragment.CONTENT_OFFSET
                    || StringUtils.equals(previousQrCodeRecv, qrCode)) {
                sleep();
                continue;
            }
            Fragment fragReceived = Fragment.parseQrCode(qrCode);
            if (previousFragSent == null
                    || StringUtils.equals(fragReceived.getPreviousChecksum(), previousFragSent.getChecksum())
                    || fragReceived.getFlag() == Flag.RESET) {
                previousQrCodeRecv = qrCode;
                previousFragRecv = fragReceived;
                return fragReceived;
            }
            sleep();
        }
        return null;
    }

    private void sleep() {
        try {
            Thread.sleep(SLEEP_INTERVAL);
        } catch (Exception e) {
        }
    }

}
