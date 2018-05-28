package org.barsf.signer;

import org.apache.commons.lang3.StringUtils;
import org.barsf.iota.lib.utils.Unsigned;
import org.barsf.signer.exception.IncompatibleVersionException;
import org.barsf.signer.exception.PeerResetException;
import org.barsf.signer.exception.ReadTimeoutException;
import org.barsf.signer.exception.SystemBusyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.concurrent.locks.ReentrantLock;

import static org.barsf.signer.misc.Command.SIGN_MILESTONE;

public class Online extends Base {

    public static final int COMMAND_OFFSET = 0;
    public static final int COMMAND_LENGTH = 1;
    public static final int MS_INDEX_OFFSET = COMMAND_OFFSET + COMMAND_LENGTH;
    public static final int MS_INDEX_LENGTH = String.valueOf(0x01 << 22).length();
    public static final int MS_HASH_TRYTES_OFFSET = MS_INDEX_OFFSET + MS_INDEX_LENGTH;
    private static final Logger logger = LoggerFactory.getLogger(Online.class);
    private static final ReentrantLock LOCK = new ReentrantLock();
    private static Online ONLINE;

    private Online() {
        super(Mode.ONLINE);
    }

    public static synchronized Online getInstance() {
        if (ONLINE == null) {
            ONLINE = new Online();
        }
        return ONLINE;
    }

    public String[] signMileStone(String hashTrytes, int msIndex)
            throws ReadTimeoutException,
            PeerResetException,
            SystemBusyException,
            IncompatibleVersionException {
        String command = SIGN_MILESTONE.commandCode() + StringUtils.leftPad(msIndex + "", MS_INDEX_LENGTH, '0') + Unsigned.u27To10(Unsigned.trytes(hashTrytes)).toString();
        String response;
        boolean hasLocked = LOCK.tryLock();
        if (hasLocked) {
            try {
                response = sendAndReceive(command);
                sendAck();
            } finally {
                LOCK.unlock();
            }
        } else {
            throw new SystemBusyException();
        }
        String sign = response.substring(0, Offline.MS_SIGN_LENGTH);
        String signTrytes = Unsigned.trytes(Unsigned.u10To27(new BigInteger(sign)));
        String path = response.substring(Offline.MS_PATH_OFFSET);
        String pathTrytes = Unsigned.trytes(Unsigned.u10To27(new BigInteger(path)));
        return new String[]{signTrytes, pathTrytes};
    }

    public void reset()
            throws SystemBusyException {
        boolean hasLocked = LOCK.tryLock();
        if (hasLocked) {
            while (true) {
                try {
                    sendResetAndGetAck();
                    break;
                } catch (Exception e) {
                    logger.error("", e);
                }
            }
            LOCK.unlock();
        } else {
            throw new SystemBusyException();
        }
    }

}
