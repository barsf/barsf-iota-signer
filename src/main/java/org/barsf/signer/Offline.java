package org.barsf.signer;

import com.iota.iri.hash.Sponge;
import com.iota.iri.hash.SpongeFactory;
import org.apache.commons.lang3.StringUtils;
import org.barsf.iota.lib.Security;
import org.barsf.iota.lib.hash.Hash;
import org.barsf.iota.lib.hash.Key;
import org.barsf.iota.lib.hash.MerkleTree;
import org.barsf.iota.lib.utils.Signed;
import org.barsf.iota.lib.utils.Unsigned;
import org.barsf.signer.exception.PeerProcessException;
import org.barsf.signer.exception.PeerResetException;
import org.barsf.signer.exception.ReadTimeoutException;
import org.barsf.signer.qrcode.BaseTransact;
import org.barsf.signer.qrcode.Command;
import org.barsf.signer.qrcode.address.AddressRequest;
import org.barsf.signer.qrcode.address.AddressResponse;
import org.barsf.signer.qrcode.milestone.MileStoneRequest;
import org.barsf.signer.qrcode.milestone.MileStoneResponse;
import org.barsf.signer.qrcode.sign.SignRequest;
import org.barsf.signer.qrcode.sign.SignResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.barsf.signer.Online.COMMAND_LENGTH;
import static org.barsf.signer.Online.COMMAND_OFFSET;

public class Offline extends Base {

    private static final Logger logger = LoggerFactory.getLogger(Offline.class);
    private static Offline OFFLINE = null;

    private String[] seeds;

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
                } catch (PeerProcessException e) {
                    response = null;
                    sendErr();
                } catch (Exception e) {
                    logger.error("", e);
                }
            }
        }).start();
    }

    private String process(String input) throws PeerProcessException {
        try {
            Command command = Command.values()[Integer.parseInt(input.substring(COMMAND_OFFSET, COMMAND_LENGTH))];
            BaseTransact response;
            switch (command) {
                case SIGN_MILESTONE:
                    MileStoneRequest msRequest = new MileStoneRequest();
                    msRequest.parseFrom(input);
                    String sign = signMileStone(msRequest.getContent(), msRequest.getNodeIndex());
                    String path = getMerklePath(msRequest.getNodeIndex());
                    response = new MileStoneResponse();
                    ((MileStoneResponse) response).setSign(Unsigned.u27To10(Unsigned.trytes(sign)));
                    ((MileStoneResponse) response).setPath(Unsigned.u27To10(Unsigned.trytes(path)));
                    break;
                case ADDRESS:
                    AddressRequest addrRequest = new AddressRequest();
                    addrRequest.parseFrom(input);
                    response = new AddressResponse();
                    List<BigInteger> addresses = new ArrayList<>();
                    address(addrRequest.getSeedIndex(), addrRequest.getFromIndex(), addrRequest.getToIndex(), addrRequest.getSecurity())
                            .forEach(address -> addresses.add(Unsigned.u27To10(Unsigned.trytes(address))));
                    ((AddressResponse) response).setAddresses(addresses);
                    break;
                case SIGN_TRANSACTIONS:
                    SignRequest signRequest = new SignRequest();
                    signRequest.parseFrom(input);
                    sign = sign(signRequest.getSeedIndex(), signRequest.getAddressIndex(), signRequest.getSecurity(), signRequest.getContent());
                    response = new SignResponse();
                    ((SignResponse) response).setSignature(Unsigned.u27To10(Unsigned.trytes(sign)));
                    break;
                default:
                    throw new RuntimeException("unsupported command " + command);
            }
            return response.toQrCode();
        } catch (Exception e) {
            logger.error("", e);
            throw new PeerProcessException();
        }
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

    private List<String> address(int seedIndex, int fromIndex, int toIndex, int security) {
        List<String> returnValue = new ArrayList<>();
        Sponge sponge = SpongeFactory.create(SpongeFactory.Mode.KERL);
        for (int i = fromIndex; i <= toIndex; i++) {
            int[] privateKey = Key.keyFromSeed(seeds[seedIndex], i, Security.values()[security - 1], sponge);
            int[] pubKey = Key.toPubkey(privateKey, sponge);
            String addr = Key.address(pubKey, sponge);
            returnValue.add(addr);
        }
        return returnValue;
    }

    private String sign(int seedIndex, int addressIndex, int security, BigInteger unsignTrytes) {
        Sponge sponge = SpongeFactory.create(SpongeFactory.Mode.KERL);
        int[] privateKey = Key.keyFromSeed(seeds[seedIndex], addressIndex, Security.values()[security - 1], sponge);
        String trytes = Unsigned.trytes(Unsigned.u10To27(unsignTrytes));
        int[] signTrits = Hash.sign(Signed.trytes(trytes), privateKey, sponge);
        return Signed.trytes(Signed.s3to27(signTrits));
    }

    public String[] getSeeds() {
        return seeds;
    }

    public void setSeeds(String[] seeds) {
        this.seeds = seeds;
    }
}
