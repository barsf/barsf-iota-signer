package org.barsf.camera.main;

import java.awt.*;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JTextArea;

import com.google.zxing.common.GlobalHistogramBinarizer;
import org.barsf.camera.webcam.Webcam;
import org.barsf.camera.webcam.WebcamResolution;
import org.barsf.camera.webcam.WebcamPanel;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;

public class Camera extends JFrame {// implements Runnable, ThreadFactory

    private static final long serialVersionUID = 6441489157408381878L;


    private static Webcam webcam = null;
    private static WebcamPanel panel = null;
    private static JTextArea textarea = null;
    private static String result = null;
    public void DrawSth(String a){
        textarea.setText(a);
    }
    public Camera() {

        super();
        System.setProperty("apple.eawt.quitStrategy", "CLOSE_ALL_WINDOWS");
        setLayout(new FlowLayout());
        setTitle("Read QR / Bar Code With Webcam");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Dimension size = WebcamResolution.HD.getSize();

        webcam = Webcam.getDefault();
        // System.out.println("Device -> ");
        webcam.setViewSize(size);


        panel = new WebcamPanel(webcam);
        panel.setPreferredSize(size);
        panel.setFPSDisplayed(true);

        textarea = new JTextArea();
        textarea.setEditable(false);
        textarea.setPreferredSize(size);

        add(panel);
        add(textarea);

        pack();
        setVisible(false);
    }

    public String scan(){

        String result = null;
        BufferedImage image = null;
        int retry_cnt = 10;
        do{
            if(retry_cnt-- < 0){
                break;
            }
            if (webcam.isOpen()) {
                if ((image = webcam.getImage()) == null) {
                    // System.out.println("webcam.getImage() == null");
                    continue;
                }

                LuminanceSource source = new BufferedImageLuminanceSource(image);
                //BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
                BinaryBitmap bitmap = new BinaryBitmap(new GlobalHistogramBinarizer(source));
                int h = bitmap.getHeight();
                int w = bitmap.getWidth();
                int crop = (w-h)/2;
                bitmap = bitmap.crop(crop, 0, w - crop, h);

                try {
                    Result r = new MultiFormatReader().decode(bitmap);
                    if(r != null)
                        result = r.getText();

                } catch (NotFoundException e) {
                    // fall thru, it means there is no QR code in image
                }
            }else{
                webcam.close();
                panel.stop();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                panel.start();
                break;
            }

        }while(result == null);

        return result;

    }
    public void destroy(){
        if (webcam.isOpen()) {
            panel.stop();
        }
    }
    public void reinit(){
        if(panel.isStarting() || panel.isStarted()){
            panel.stop();
        }
        panel.start();
    }
}