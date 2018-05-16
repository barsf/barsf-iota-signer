package org.barsf.signer;

import com.google.zxing.WriterException;
import org.barsf.signer.exception.ReadTimeoutException;

public class Online extends Base {

    public static void main(String[] args) throws Exception {
        Online online = new Online();
        online.initial();
        String message = "1";
        while (true) {
            try {
                message = online.sendAndReceive(message) + 11;
                if (message.length() % 1373 < 2) {
                    online.printLengthScore();
                    System.out.println("message.length() = " + message.length());
                }
            } catch (ReadTimeoutException e) {
                online.initial();
            } catch (WriterException e) {
                e.printStackTrace();
            }
        }
    }

    public void initial() {
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
