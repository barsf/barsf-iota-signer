package org.barsf.screen.main;

import com.google.zxing.WriterException;

import java.util.Random;

public class DisplayTest {
    public static void main(String[] args){

        String dict = "0987654321";
        StringBuffer text = new StringBuffer();
        Screen scr = new Screen();
        for(int i = 0; i < 10000000; i++){
            Random rand = new Random();
            int cnt = rand.nextInt(100+1) + 600;
            cnt = 1024;
            for(int j = 0 ; j < cnt; j++){
                Random rand1 = new Random();
                int index = rand1.nextInt(dict.length() - 1 - 0 + 1) + 0;
                text.append(dict.charAt(index));
            }

            try {
                scr.display(text.toString());
                text.setLength(0);
                Thread.sleep((1000));
            } catch (WriterException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
