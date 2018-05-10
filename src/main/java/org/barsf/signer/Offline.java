package org.barsf.signer;

import com.google.zxing.WriterException;
import org.apache.commons.lang3.StringUtils;
import org.barsf.signer.exception.CommunicationInterruptedException;
import org.barsf.signer.exception.FragmentTooLongException;
import org.barsf.signer.exception.TimeoutException;
import org.barsf.signer.misc.Base;

public class Offline extends Base {

    public static void main(String[] args) throws Exception {
        Offline offline = new Offline();
        String message = null;
        while (StringUtils.isEmpty(message)) {
            message = offline.receive();
        }
        message += 1;
        while (true) {
            try {
                message = offline.send(message) + 1;
            } catch (FragmentTooLongException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
            } catch (CommunicationInterruptedException e) {
                e.printStackTrace();
            } catch (WriterException e) {
                e.printStackTrace();
            }
        }
    }

}
