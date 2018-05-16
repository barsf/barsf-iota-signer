package org.barsf.signer;

import com.google.zxing.WriterException;
import org.barsf.signer.exception.PeerResetException;
import org.barsf.signer.exception.ReadTimeoutException;

public class Offline extends Base {

    public static void main(String[] args) {
        Offline offline = new Offline();
        String message = null;
        while (true) {
            try {
                try {
                    message = offline.sendAndReceive(message) + 11;
                    if (message.length() % 1373 < 2) {
                        offline.printLengthScore();
                    }
                } catch (ReadTimeoutException e) {
                    message = offline.sendAndReceive(null);
                } catch (WriterException e) {
                    e.printStackTrace();
                } catch (PeerResetException e) {
                    offline.sendResetAck();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
