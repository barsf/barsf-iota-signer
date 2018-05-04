package org.barsf.signer;


import com.google.zxing.WriterException;
import org.apache.commons.lang3.StringUtils;

public class Offline extends Base {

    String previousInput = null;

    public static void main(String[] args) throws WriterException {
        Offline offline = new Offline();
        while (true) {
            offline.process();
            offline.sleep();
        }
    }

    public void process() throws WriterException {
        String input = camera.scan();
        if (!StringUtils.equals(input, previousInput)) {
            int command = Integer.parseInt(input);
            screen.display(String.valueOf(++command));
            previousInput = input;
        }
    }

}
