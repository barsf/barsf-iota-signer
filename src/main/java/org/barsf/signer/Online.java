package org.barsf.signer;

import com.google.zxing.WriterException;
import org.apache.commons.lang3.StringUtils;

public class Online extends Base {

    String previousInput = null;

    public static void main(String[] args) throws WriterException {
        Online online = new Online();
        online.initial();
        while (true) {
            online.process();
            online.sleep();
        }
    }

    public void initial() throws WriterException {
        screen.display("0");
    }

    public void process() throws WriterException {
        String input = camera.scan();
        if (!StringUtils.equals(input, previousInput)) {
            System.out.println(input);
            int command = Integer.parseInt(input);
            screen.display(String.valueOf(++command));
            previousInput = input;
        }
    }

}
