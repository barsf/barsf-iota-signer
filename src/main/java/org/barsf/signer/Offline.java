package org.barsf.signer;

import com.google.zxing.WriterException;
import org.barsf.signer.exception.PeerResetException;
import org.barsf.signer.exception.ReadTimeoutException;

import java.util.Random;

public class Offline extends Base {

    public Offline(Type type) {
        super(type);
    }

    public static void main(String[] args) {
        Offline offline = new Offline(Type.OFFLINE);
        String message = null;
        Random random = new Random();
        while (true) {
            try {
                try {
                    message = offline.sendAndReceive(message) + Math.abs(random.nextInt());
                    System.out.println("message.length() = " + message.length());
                } catch (ReadTimeoutException | WriterException e) {
                    System.out.println("ops, this should never happens");
                } catch (PeerResetException e) {
                    offline.sendResetAck();
                    message = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
