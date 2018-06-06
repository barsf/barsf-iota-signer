package org.barsf.signer;

import org.barsf.iota.lib.utils.Unsigned;
import org.barsf.signer.exception.*;
import org.barsf.signer.qrcode.address.AddressRequest;
import org.barsf.signer.qrcode.address.AddressResponse;
import org.barsf.signer.qrcode.milestone.MileStoneRequest;
import org.barsf.signer.qrcode.milestone.MileStoneResponse;
import org.barsf.signer.qrcode.sign.SignRequest;
import org.barsf.signer.qrcode.sign.SignResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.concurrent.locks.ReentrantLock;

public class Online extends Base {

    public static final int COMMAND_OFFSET = 0;
    public static final int COMMAND_LENGTH = 1;
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

    public MileStoneResponse signMs(int treeIndex, int nodeIndex, String hashTrytes)
            throws ReadTimeoutException, PeerResetException, SystemBusyException, IncompatibleVersionException, PeerProcessException {
        MileStoneRequest request = new MileStoneRequest();
        request.setTreeIndex(treeIndex);
        request.setNodeIndex(nodeIndex);
        request.setContent(Unsigned.u27To10(Unsigned.trytes(hashTrytes)));
        MileStoneResponse response = new MileStoneResponse();
        response.parseFrom(transact(request.toQrCode()));
        return response;
    }

    public AddressResponse address(int seedIndex, int fromIndex, int toIndex, int security)
            throws ReadTimeoutException, SystemBusyException, IncompatibleVersionException, PeerResetException, PeerProcessException {
        AddressRequest request = new AddressRequest();
        request.setSeedIndex(seedIndex);
        request.setFromIndex(fromIndex);
        request.setToIndex(toIndex);
        request.setSecurity(security);
        AddressResponse response = new AddressResponse();
        response.parseFrom(transact(request.toQrCode()));
        return response;
    }

    public SignResponse sign(int seedIndex, int addressIndex, int security, String hashTrytes)
            throws PeerProcessException, ReadTimeoutException, SystemBusyException, IncompatibleVersionException, PeerResetException {
        SignRequest request = new SignRequest();
        request.setSeedIndex(seedIndex);
        request.setAddressIndex(addressIndex);
        request.setSecurity(security);
        request.setContent(Unsigned.u27To10(Unsigned.trytes(hashTrytes)));
        SignResponse response = new SignResponse();
        response.parseFrom(transact(request.toQrCode()));
        return response;
    }

    private String transact(String qrCode)
            throws SystemBusyException, ReadTimeoutException, PeerResetException, IncompatibleVersionException, PeerProcessException {
        String response;
        boolean hasLocked = LOCK.tryLock();
        if (hasLocked) {
            try {
                response = sendAndReceive(qrCode);
                sendAck();
            } finally {
                LOCK.unlock();
            }
        } else {
            throw new SystemBusyException();
        }
        return response;
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
