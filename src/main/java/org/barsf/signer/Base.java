package org.barsf.signer;

import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.qrcode.encoder.Encoder;
import com.google.zxing.qrcode.encoder.QRCode;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.barsf.camera.Camera;
import org.barsf.screen.Screen;
import org.barsf.signer.exception.*;
import org.barsf.signer.misc.Flag;
import org.barsf.signer.misc.Segment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.TreeMap;

import static org.barsf.screen.Screen.HINTS;
import static org.barsf.signer.misc.Segment.CONTENT_OFFSET;


public abstract class Base {

    private static final int TIME_OUT_UPON_OP = 10 * 1000;
    private static final int TIMEOUT_UPON_NONE_OP = 2 * 1000;

    private static final double SCORE_INCREASE_RATE = 0.99D;
    private static final double SCORE_INCREASE_RATE_PLUS = 10 - 10 * SCORE_INCREASE_RATE;

    private static final double PROMPT_SCORE = 5 * Math.pow(SCORE_INCREASE_RATE, 3)
            + SCORE_INCREASE_RATE_PLUS * Math.pow(SCORE_INCREASE_RATE, 2)
            + SCORE_INCREASE_RATE_PLUS * Math.pow(SCORE_INCREASE_RATE, 1)
            + SCORE_INCREASE_RATE_PLUS;
    private static final ArrayList<Integer> LENGTH_LIST = new ArrayList<>();
    private static final Logger logger = LoggerFactory.getLogger(Base.class);

    static {
        int previousVersion = getVersion(StringUtils.repeat('0', Segment.CONTENT_OFFSET + 1));
        for (int i = Segment.CONTENT_OFFSET + 1; i <= Segment.MAX_SEGMENT_LENGTH; i++) {
            int currentVersion = getVersion(StringUtils.repeat('0', i));
            if (currentVersion != previousVersion) {
                LENGTH_LIST.add(i - 1);
                previousVersion = currentVersion;
            }
        }
        if (LENGTH_LIST.get(LENGTH_LIST.size() - 1) != Segment.MAX_SEGMENT_LENGTH) {
            LENGTH_LIST.add(Segment.MAX_SEGMENT_LENGTH);
        }
    }

    private Screen screen;
    private Camera camera;
    private Segment previousSegSent = null;
    private Segment previousSegRecv = null;
    private String previousQrCodeRecv = null;
    private int currentSegmentLenLimit;
    private TreeMap<Integer, Double> lengthScore;
    private Mode mode = null;

    protected Base(Mode mode) {
        this.screen = new Screen();
        this.camera = new Camera();
        lengthScore = new TreeMap<>();
        this.mode = mode;
        LENGTH_LIST.forEach(length -> lengthScore.put(length, 5.0D));
        currentSegmentLenLimit = lengthScore.firstKey();
    }

    private static int getVersion(String text) {
        try {
            QRCode code = Encoder.encode(text,
                    (ErrorCorrectionLevel) HINTS.get(EncodeHintType.ERROR_CORRECTION),
                    HINTS);
            return code.getVersion().getVersionNumber();
        } catch (WriterException e) {
            throw new RuntimeException(e);
        }
    }

    private void reset() {
        this.previousSegSent = null;
        this.previousSegRecv = null;
    }

    protected String sendAndReceive(String command)
            throws PeerResetException, ReadTimeoutException, IncompatibleVersionException, PeerProcessException {
        boolean hasMoreSegment = true;
        StringBuilder fullResponse = new StringBuilder();

        sendAndReceive:
        while (StringUtils.length(command) > 0 || hasMoreSegment) {
            Segment request = null;
            int timeout;
            if (command != null) {
                request = new Segment();
                if (command.length() > currentSegmentLenLimit - CONTENT_OFFSET) {
                    request.setFlag(Flag.NEXT);
                    timeout = TIMEOUT_UPON_NONE_OP;
                } else {
                    request.setFlag(Flag.LAST);
                    timeout = TIME_OUT_UPON_OP;
                }
                request.setFragmentContent(StringUtils.substring(command, 0, currentSegmentLenLimit - CONTENT_OFFSET));
                write(request);
            } else {
                timeout = TIME_OUT_UPON_OP;
            }

            long startTime = System.currentTimeMillis();
            Segment response;

            while (true) {
                try {
                    response = readNext();
                    if (request != null) {
                        if (!StringUtils.equals(request.getVersion(), response.getVersion())) {
                            throw new IncompatibleVersionException();
                        }
                        currentSegmentLenLimit = nextFragmentMaxLength(request.toQrCode().length(), true);
                    }
                    break;
                } catch (QrCodeNotFundException e) {
                    if (System.currentTimeMillis() - startTime > 10 * timeout) {
                        if (this.mode == Mode.ONLINE) {
                            throw new ReadTimeoutException();
                        }
                    }
                } catch (PeerNoResponseException e) {
                    if (System.currentTimeMillis() - startTime > 2 * timeout) {
                        if (this.mode == Mode.ONLINE) {
                            throw new ReadTimeoutException();
                        }
                    } else if (System.currentTimeMillis() - startTime > timeout) {
                        if (request == null
                                || request.toQrCode().length() <= lengthScore.firstKey()) {
                            if (this.mode == Mode.ONLINE) {
                                throw new ReadTimeoutException();
                            }
                        } else {
                            currentSegmentLenLimit = nextFragmentMaxLength(request.toQrCode().length(), false);
                            continue sendAndReceive;
                        }
                    }
                } catch (CommunicationInterruptedException e) {
                    if (System.currentTimeMillis() - startTime > timeout) {
                        if (this.mode == Mode.ONLINE) {
                            throw new ReadTimeoutException();
                        }
                    }
                }
            }

            switch (response.getFlag()) {
                case RESET:
                    throw new PeerResetException();
                case ERR:
                    if (this.mode == Mode.ONLINE) {
                        throw new PeerProcessException();
                    }
                    break;
                case LAST:
                    hasMoreSegment = false;
                    fullResponse.append(response.getFragmentContent());
                    break;
                case NEXT:
                    hasMoreSegment = true;
                    if (command == null) command = "";
                    fullResponse.append(response.getFragmentContent());
                    break;
            }

            if (command != null && request != null) {
                command = StringUtils.substring(command, request.getFragmentContent().length());
            }
        }
        return fullResponse.toString();
    }

    protected void sendResetAndGetAck()
            throws Exception {
        logger.info("sending reset");
        reset();
        screen.toFront();
        Segment segment = new Segment();
        segment.setFlag(Flag.RESET);
        write(segment);
        long startTime = System.currentTimeMillis();
        while (true) {
            try {
                readNext();
                break;
            } catch (Exception e) {
                if (System.currentTimeMillis() - startTime > TIME_OUT_UPON_OP) {
                    throw e;
                }
            }
        }
    }

    protected void sendAck() {
        // warning: do NOT invoke reset method
        logger.info("sending ack");
        screen.toFront();
        Segment segment = new Segment();
        segment.setFlag(Flag.LAST);
        write(segment);
    }

    protected void sendErr() {
        // warning: do NOT invoke reset method
        logger.info("sending err");
        Segment segment = new Segment();
        segment.setFlag(Flag.ERR);
        write(segment);
    }

    private void write(Segment segment) {
        if (segment != null) {
            segment.newNonce();
            if (previousSegRecv != null) {
                segment.setPreviousChecksum(previousSegRecv.getChecksum());
            }
            try {
                screen.display(segment.toQrCode());
            } catch (WriterException e) {
                logger.error("ops, this should never happens", e);
            }
            previousSegSent = segment;
        }
    }

    private Segment readNext()
            throws QrCodeNotFundException, PeerNoResponseException, CommunicationInterruptedException {
        String qrCode = camera.scan();
        if (StringUtils.isBlank(qrCode)
                || qrCode.length() < CONTENT_OFFSET) {
            throw new QrCodeNotFundException();
        }
        if (StringUtils.equals(previousQrCodeRecv, qrCode)) {
            throw new PeerNoResponseException();
        }
        Segment fragReceived = Segment.parseQrCode(qrCode);
        if (previousSegSent == null
                || StringUtils.equals(fragReceived.getPreviousChecksum(), previousSegSent.getChecksum())
                || fragReceived.getFlag() == Flag.RESET) {
            previousQrCodeRecv = qrCode;
            previousSegRecv = fragReceived;
            return fragReceived;
        } else {
            throw new CommunicationInterruptedException();
        }
    }

    private int onFail(int theKey) {
        double theScore = lengthScore.get(theKey);
        lengthScore.put(theKey, theScore * SCORE_INCREASE_RATE / 2);
        // logger.info("score : {} =-> {}", theKey, lengthScore.get(theKey));
        if (lengthScore.lowerKey(theKey) != null) {
            // logger.info("try to shorter from {} to {}", theKey, lengthScore.lowerKey(theKey));
            return lengthScore.lowerKey(theKey);
        } else {
            return theKey;
        }
    }

    private int onSuccess(int theKey) {
        double score = lengthScore.get(theKey);
        lengthScore.put(theKey, score * SCORE_INCREASE_RATE + SCORE_INCREASE_RATE_PLUS);
        // logger.info("score : {} -=> {}", theKey, lengthScore.get(theKey));
        if (score >= PROMPT_SCORE) {
            while (lengthScore.higherKey(theKey) != null) {
                Integer nextKey = lengthScore.higherKey(theKey);
                double nextScore = lengthScore.get(nextKey);
                if (nextScore > PROMPT_SCORE) {
                    theKey = nextKey;
                } else if (nextScore >= 0.5D) {
                    return nextKey;
                } else {
                    lengthScore.put(nextKey, nextScore * 1.001);
                    break;
                }
            }
        }
        return theKey;
    }

    private int nextFragmentMaxLength(int length, boolean isSuccess) {
        Integer theKey = (lengthScore.containsKey(length) ? length : lengthScore.higherKey(length));
        theKey = ObjectUtils.defaultIfNull(theKey, lengthScore.lastKey());
        // lengthScore must contain the key
        int newLength;
        if (isSuccess) {
            newLength = onSuccess(theKey);
            logger.info("{} -=> {}", length, newLength);
        } else {
            newLength = onFail(theKey);
            logger.info("{} =-> {}", length, newLength);
        }
        return newLength;
    }

    public Mode getMode() {
        return mode;
    }

    public enum Mode {
        ONLINE, OFFLINE
    }
}
