package org.barsf.signer;

import org.barsf.signer.exception.PeerResetException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class Online extends Base {

    private static final Logger logger = LoggerFactory.getLogger(Online.class);

    public Online(Type type) {
        super(type);
    }

    public static void main(String[] args) {
        Online online = new Online(Type.ONLINE);
        online.reset();
        String message = "";
        Random random = new Random();
        while (true) {
            try {
                message = online.sendAndReceive(message) + Math.abs(random.nextInt());
                logger.info("message.length() = {}", message.length());
            } catch (PeerResetException e) {
                logger.info("ops, this should never happens");
            } catch (Exception e) {
                logger.error("", e);
                online.reset();
            }
        }
    }

    public void reset() {
        while (true) {
            try {
                sendResetAndGetAck();
                break;
            } catch (Exception e) {
                logger.error("", e);
            }
        }
    }

}
