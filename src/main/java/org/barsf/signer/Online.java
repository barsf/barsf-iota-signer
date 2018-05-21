package org.barsf.signer;

import org.barsf.signer.exception.PeerResetException;

import java.util.Random;

public class Online extends Base {

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
                System.out.println("message.length() = " + message.length());
            } catch (PeerResetException e) {
                System.out.println("ops, this should never happens");
            } catch (Exception e) {
                e.printStackTrace();
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
                e.printStackTrace();
            }
        }
    }

}
