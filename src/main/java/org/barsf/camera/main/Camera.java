package org.barsf.camera.main;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.GlobalHistogramBinarizer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class Camera {

    private static Webcam webcam = null;

    public Camera() {
        super();
        webcam = Webcam.getDefault();
        webcam.getDevice().setResolution(WebcamResolution.HD.getSize());
        webcam.setViewSize(WebcamResolution.HD.getSize());
        webcam.open(true);
    }

    public String scan() {
        if (webcam.isOpen() && webcam.isImageNew()) {
            BufferedImage image = webcam.getImage();
            if (image == null) {
                return null;
            }

            LuminanceSource source = new BufferedImageLuminanceSource(image);
            BinaryBitmap bitmap = new BinaryBitmap(new GlobalHistogramBinarizer(source));

            try {
                Result r = new MultiFormatReader().decode(bitmap);
                if (r != null) {
                    return r.getText();
                }
            } catch (NotFoundException e) {
                return null;
            }
        }
        return null;
    }

}