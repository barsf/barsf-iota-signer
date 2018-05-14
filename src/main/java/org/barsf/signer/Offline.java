package org.barsf.signer;

import com.google.zxing.WriterException;
import org.barsf.signer.exception.PeerResetException;
import org.barsf.signer.exception.TimeoutException;

import java.util.Random;

public class Offline extends Base {

    public static void main(String[] args) {
        Offline offline = new Offline();
        String message = offline.initial();
        while (true) {
            try {
                try {
                    message = offline.sendAndReceive(message) + Math.abs(new Random().nextInt());
                } catch (TimeoutException e) {
                    e.printStackTrace();
                } catch (WriterException e) {
                    e.printStackTrace();
                } catch (PeerResetException e) {
                    message = offline.initial();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String initial() {
        while (true) {
            try {
                return sendAndReceive(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
