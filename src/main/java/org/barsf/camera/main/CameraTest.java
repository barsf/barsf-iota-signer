package org.barsf.camera.main;


import org.barsf.signer.util.MD5;

import java.util.concurrent.*;

public class CameraTest {


    public static void main(String[] args) {
        Camera c = new Camera();
        int i = 0;
        while(true){
            String res = c.scan();
            System.out.println("Scan Result -> " + res);
            if(res != null)
                c.DrawSth(res.length() + " - " + MD5.md5(res) + "["+(++i)+"]");
            else
                c.DrawSth("----");
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
                c.DrawSth(e.getLocalizedMessage());
            }
        }
    }
}