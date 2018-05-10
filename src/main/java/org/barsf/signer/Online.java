package org.barsf.signer;

import com.google.zxing.WriterException;
import org.barsf.signer.exception.CommunicationInterruptedException;
import org.barsf.signer.exception.FragmentTooLongException;
import org.barsf.signer.exception.TimeoutException;
import org.barsf.signer.misc.Base;

public class Online extends Base {

    public static void main(String[] args) throws Exception {
        Online online = new Online();
        online.initial();
        String message = "1";
        while (true) {
            try {
                message = online.send(message) + 1;
            } catch (FragmentTooLongException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
            } catch (CommunicationInterruptedException e) {
                online.sendResetAndAck();
            } catch (WriterException e) {
                e.printStackTrace();
            }
        }
    }

    public void initial() throws Exception {
        sendResetAndAck();
        System.out.println("online initialed");
    }

}
