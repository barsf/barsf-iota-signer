package org.barsf.signer;

import com.google.zxing.WriterException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.barsf.camera.main.Camera;
import org.barsf.screen.main.Screen;
import org.barsf.signer.exception.PeerResetException;
import org.barsf.signer.exception.ReadTimeoutException;
import org.barsf.signer.misc.Flag;
import org.barsf.signer.misc.Fragment;

import java.util.Arrays;
import java.util.Comparator;
import java.util.TreeMap;

import static org.barsf.signer.misc.Fragment.CONTENT_OFFSET;


public abstract class Base {

    private static final int TIME_OUT_UPON_OP = 5 * 1000;
    private static final int TIME_OUT_UPON_NO_OP = 2000;
    private static final int TIME_OUT_OVERALL = 10 * 1000;

    // private static final double LOWER_SCORE = 5 * Math.pow(0.9D, 3.0d);
    private static final double PROMPT_SCORE = 5 * Math.pow(0.9D, 3.0d) + Math.pow(0.9D, 2.0d) + Math.pow(0.9D, 1.0d) + 1;

    private Screen screen;
    private Camera camera;

    private Fragment previousFragSent = null;
    private Fragment previousFragRecv = null;

    private String previousQrCodeRecv = null;

    private int currentFragLen;
    private TreeMap<Integer, Double> lengthScore;

    protected Base() {
        this.screen = new Screen();
        this.camera = new Camera();
        lengthScore = new TreeMap<>();
        int[] fragLengths = new int[]{Fragment.FRAGMENT_MAX_LENGTH};
        while (true) {
            int nextLength = fragLengths[0] / 2;
            if (nextLength >= CONTENT_OFFSET) {
                fragLengths = ArrayUtils.insert(0, fragLengths, nextLength);
            } else {
                break;
            }
        }
        Arrays.stream(fragLengths).forEach(length -> lengthScore.put(length, 5.0D));
        currentFragLen = lengthScore.firstKey();
    }

    private void reset() {
        this.previousFragSent = null;
        this.previousFragRecv = null;
    }

    protected String sendAndReceive(String command)
            throws WriterException, PeerResetException, ReadTimeoutException {
        boolean needNextResponse = true;
        StringBuilder fullResponse = new StringBuilder();
        long startTime = System.currentTimeMillis();
        while (StringUtils.length(command) > 0 || needNextResponse) {
            Fragment output = null;
            int timeout = 0;
            if (command != null) {
                output = new Fragment();
                if (command.length() > currentFragLen - CONTENT_OFFSET) {
                    output.setFlag(Flag.NEXT);
                    timeout = TIME_OUT_UPON_NO_OP;
                } else {
                    output.setFlag(Flag.LAST);
                    timeout = TIME_OUT_UPON_OP;
                }
                output.setFragmentContent(StringUtils.substring(command, 0, currentFragLen - CONTENT_OFFSET));
                write(output);
            } else {
                timeout = TIME_OUT_UPON_OP;
            }
            Fragment input;
            try {
                input = readNext(timeout);
                startTime = System.currentTimeMillis();
                if (output != null) {
                    currentFragLen = nextFragmentMaxLength(output.getFragmentContent().length() + CONTENT_OFFSET, true);
                }
            } catch (ReadTimeoutException e) {
                if (output != null) {
                    currentFragLen = nextFragmentMaxLength(output.getFragmentContent().length() + CONTENT_OFFSET, false);
                }
                if (System.currentTimeMillis() - startTime > TIME_OUT_OVERALL) {
                    throw e;
                } else {
                    continue;
                }
            }
            switch (input.getFlag()) {
                case RESET:
                    System.out.println("remote reset");
                    throw new PeerResetException();
                case LAST:
                    needNextResponse = false;
                    fullResponse.append(input.getFragmentContent());
                    break;
                case NEXT:
                    needNextResponse = true;
                    command = "";
                    fullResponse.append(input.getFragmentContent());
                    break;
            }
            if (command != null && output != null) {
                command = StringUtils.substring(command, output.getFragmentContent().length());
            }
        }
        return fullResponse.toString();
    }

    protected void sendResetAndGetAck()
            throws WriterException, ReadTimeoutException {
        System.out.println("sending reset");
        reset();
        Fragment fragment = new Fragment();
        fragment.setFlag(Flag.RESET);
        write(fragment);
        readNext(TIME_OUT_UPON_NO_OP);
    }

    protected void sendResetAck() throws WriterException {
        // warning: do NOT invoke reset method
        Fragment fragment = new Fragment();
        fragment.setFlag(Flag.LAST);
        write(fragment);
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

    private Fragment readNext(long timeout) throws ReadTimeoutException {
        long beginTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - beginTime < timeout) {
            String qrCode = camera.scan();
            if (StringUtils.isBlank(qrCode)
                    || qrCode.length() < CONTENT_OFFSET) {
                continue;
            }
            if (StringUtils.equals(previousQrCodeRecv, qrCode)) {
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
        }
        throw new ReadTimeoutException();
    }

    private int onFail(int length) {
        int newLength = length / 2;
        int minLength = NumberUtils.max(CONTENT_OFFSET, Fragment.FRAGMENT_MAX_LENGTH / 32);
        if (newLength < minLength) {
            newLength = minLength;
        }
        System.out.println("try to shorter from " + length + " to " + newLength);
        return newLength;
    }

    private int onSuccess(int length) {
        while (lengthScore.get(length) != null
                && lengthScore.get(length) >= PROMPT_SCORE) {
            length *= 2;
        }
        if (length > Fragment.FRAGMENT_MAX_LENGTH) {
            length = Fragment.FRAGMENT_MAX_LENGTH;
        }
        return length;
    }

    private int nextFragmentMaxLength(int length, boolean isSuccess) {
        int mostNearbyPosition = 0;
        if (isSuccess) {
            mostNearbyPosition = lengthScore.keySet().stream().filter(l -> l <= length).max(Comparator.comparing(Integer::valueOf)).orElse(lengthScore.firstKey());
        } else {
            mostNearbyPosition = lengthScore.keySet().stream().filter(l -> l >= length).min(Comparator.comparing(Integer::valueOf)).orElse(lengthScore.lastKey());
        }
        double score = lengthScore.getOrDefault(mostNearbyPosition, 5.0D) * 0.9D;
        if (isSuccess) score += 1.0D;
        lengthScore.put(mostNearbyPosition, score);
        if (!isSuccess) {
            return onFail(mostNearbyPosition);
        } else {
            return onSuccess(mostNearbyPosition);
        }
    }

    public void printLengthScore() {
        lengthScore.forEach((k, v) -> System.out.println(k + " : " + v));
        System.out.println("currentFragLen : " + currentFragLen);
    }

}
