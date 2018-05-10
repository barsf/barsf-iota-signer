package org.barsf.signer.misc;

import com.google.zxing.WriterException;
import org.apache.commons.lang3.StringUtils;
import org.barsf.camera.main.Camera;
import org.barsf.screen.main.Screen;
import org.barsf.signer.exception.CommunicationInterruptedException;
import org.barsf.signer.exception.FragmentTooLongException;
import org.barsf.signer.exception.TimeoutException;


public abstract class Base {

    private static final int SLEEP_INTERVAL = 50;
    private static final int RETRY_DISPLAY_MS = 5 * 1000;
    private static final int TIME_OUT_MS = 5 * 60 * 1000;

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

    protected String send(String command)
            throws FragmentTooLongException, TimeoutException, CommunicationInterruptedException, WriterException {
        System.out.println("sent    : " + command);
        if (StringUtils.isEmpty(command)) {
            throw new RuntimeException("command is empty");
        }
        Fragment lastResponse;
        boolean hasMoreResponse = true;
        StringBuilder fullResponse = new StringBuilder();
        while (command.length() > 0 || hasMoreResponse) {
            Fragment fragment = new Fragment();
            if (command.length() > Fragment.CONTENT_MAX_LENGTH) {
                fragment.setFlag(Flag.NEXT);
            } else {
                fragment.setFlag(Flag.LAST);
            }
            fragment.setFragmentContent(StringUtils.substring(command, 0, Fragment.CONTENT_MAX_LENGTH));
            lastResponse = writeAndRead(fragment);
            switch (lastResponse.getFlag()) {
                case RESET:
                    sendResetAck();
                    return null;
                case LAST:
                    hasMoreResponse = false;
                    fullResponse.append(lastResponse.getFragmentContent());
                    break;
                case NEXT:
                    hasMoreResponse = true;
                    fullResponse.append(lastResponse.getFragmentContent());
                    break;
            }
            command = StringUtils.substring(command, Fragment.CONTENT_MAX_LENGTH);
        }
        System.out.println("receive : " + fullResponse.toString());
        return fullResponse.toString();
    }

    /**
     * receive a message, if is empty means received a reset command
     *
     * @return
     * @throws Exception
     */
    protected String receive()
            throws TimeoutException, CommunicationInterruptedException, WriterException {
        StringBuilder sb = new StringBuilder();
        Fragment fragment = new Fragment();
        fragment.setFlag(Flag.LAST);
        Fragment lastResponse = writeAndRead(null);
        while (true) {
            switch (lastResponse.getFlag()) {
                case RESET:
                    sendResetAck();
                    return null;
                case LAST:
                    sb.append(lastResponse.getFragmentContent());
                    System.out.println("receive : " + sb.toString());
                    return sb.toString();
                case NEXT:
                    sb.append(lastResponse.getFragmentContent());
                    lastResponse = writeAndRead(fragment);
            }
        }
    }

    protected void sendResetAndAck()
            throws TimeoutException, CommunicationInterruptedException, WriterException {
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
            throws WriterException, TimeoutException, CommunicationInterruptedException {
        long firstSent = System.currentTimeMillis();
        long lastRetry = 0;
        while (true) {
            if (System.currentTimeMillis() - firstSent >= TIME_OUT_MS) {
                throw new TimeoutException();
            }
            if (System.currentTimeMillis() - lastRetry >= RETRY_DISPLAY_MS) {
                write(fragment);
                lastRetry = System.currentTimeMillis();
                continue;
            }
            sleep();
            String qrCode = camera.scan();
            if (StringUtils.isBlank(qrCode)
                    || qrCode.length() < Fragment.CONTENT_OFFSET
                    || StringUtils.equals(previousQrCodeRecv, qrCode)) {
                continue;
            }

            Fragment fragReceived = Fragment.parseQrCode(qrCode);
            if (previousFragSent == null
                    || StringUtils.equals(fragReceived.getPreviousChecksum(), previousFragSent.getChecksum())
                    || fragReceived.getFlag() == Flag.RESET) {
                previousQrCodeRecv = qrCode;
                previousFragRecv = fragReceived;
                return fragReceived;
            } else {
                throw new CommunicationInterruptedException();
            }
        }
    }

    private void write(Fragment fragment) throws WriterException {
        if (fragment != null) {
            fragment.setPreviousChecksum((previousFragRecv == null ?
                    StringUtils.repeat("0", Fragment.PREVIOUS_CHECKSUM_LENGTH) : previousFragRecv.getChecksum()));
            screen.display(fragment.toQrCode());
            previousFragSent = fragment;
        }
    }

    private void sleep() {
        try {
            Thread.sleep(SLEEP_INTERVAL);
        } catch (Exception e) {
        }
    }

}
