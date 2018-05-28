package org.barsf.signer;

import com.iota.iri.hash.Sponge;
import com.iota.iri.hash.SpongeFactory;
import org.apache.commons.lang3.StringUtils;
import org.barsf.iota.lib.hash.Hash;
import org.barsf.iota.lib.hash.MerkleTree;
import org.barsf.iota.lib.utils.Signed;
import org.barsf.iota.lib.utils.Unsigned;
import org.barsf.signer.exception.PeerResetException;
import org.barsf.signer.exception.ReadTimeoutException;
import org.barsf.signer.misc.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;

import static org.barsf.signer.Online.MS_INDEX_LENGTH;
import static org.barsf.signer.Online.MS_INDEX_OFFSET;

public class Offline extends Base {

    public static final int MS_SIGN_OFFSET = 0;
    public static final int MS_SIGN_LENGTH = Unsigned.u27To10(Unsigned.trytes(StringUtils.repeat('M', 2187))).toString().length();
    public static final int MS_PATH_OFFSET = MS_SIGN_OFFSET + MS_SIGN_LENGTH;
    private static final Logger logger = LoggerFactory.getLogger(Offline.class);
    private static Offline OFFLINE = null;

    private Offline() {
        super(Mode.OFFLINE);
    }

    public static Offline getInstance() {
        if (OFFLINE == null) {
            OFFLINE = new Offline();
        }
        return OFFLINE;
    }

    public void start() {
        new Thread(() -> {
            String response = null;
            while (true) {
                try {
                    String request = sendAndReceive(response);
                    if (StringUtils.length(request) != 0) {
                        response = process(request);
                    } else {
                        response = null;
                    }
                } catch (ReadTimeoutException e) {
                    logger.error("ops, this should never happens", e);
                } catch (PeerResetException e) {
                    response = null;
                    sendAck();
                } catch (Exception e) {
                    logger.error("", e);
                }
            }
        }).start();
    }

    private String process(String request) {
        Command command = Command.values()[Integer.parseInt(request.substring(0, Online.COMMAND_LENGTH))];
        String response;
        String hash;
        int msIndex;
        switch (command) {
            case SIGN_MILESTONE:
                msIndex = Integer.parseInt(request.substring(MS_INDEX_OFFSET, MS_INDEX_OFFSET + MS_INDEX_LENGTH));
                hash = request.substring(Online.MS_HASH_TRYTES_OFFSET);
                String sign = signMileStone(new BigInteger(hash), msIndex);
                String path = getMerklePath(msIndex);
                sign = Unsigned.u27To10(Unsigned.trytes(sign)).toString();
                path = Unsigned.u27To10(Unsigned.trytes(path)).toString();
                response = StringUtils.leftPad(sign, MS_SIGN_LENGTH, '0')
                        + path;
                break;
            default:
                throw new RuntimeException("unsupported command " + command);
        }
        return response;
    }


    private String signMileStone(BigInteger unsignTrytes, int msIndex) {
        Sponge sponge = SpongeFactory.create(SpongeFactory.Mode.KERL);
        String trytes = Unsigned.trytes(Unsigned.u10To27(unsignTrytes));
        int[] signTrits = Hash.sign(Signed.trytes(trytes), MerkleTree.getMerkleKey(msIndex, sponge), sponge);
        return Signed.trytes(Signed.s3to27(signTrits));
    }

    private String getMerklePath(int msIndex) {
        return MerkleTree.getMerklePath(msIndex);
    }

}
