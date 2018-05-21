package org.barsf.camera;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.GlobalHistogramBinarizer;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Vector;

public class Camera {

    private static Webcam webcam = null;
    private String previous = null;
    private static final HashMap<DecodeHintType, Object> HINTS = new HashMap<>();

    static {
        Vector<BarcodeFormat> decodeFormats = new Vector<>();
        decodeFormats.add(BarcodeFormat.QR_CODE);
        HINTS.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);
        HINTS.put(DecodeHintType.CHARACTER_SET, "UTF8");
    }

    public Camera() {
        super();
        webcam = Webcam.getDefault();
        Dimension resolution = WebcamResolution.HD.getSize();
        webcam.getDevice().setResolution(resolution);
        webcam.setViewSize(resolution);
        webcam.open(true);
    }

    public String scan() {
        if (webcam.isOpen()) {
            if (!webcam.isImageNew()) {
                return previous;
            }
            BufferedImage image = webcam.getImage();
            if (image != null) {
                LuminanceSource source = new BufferedImageLuminanceSource(image);
                BinaryBitmap bitmap = new BinaryBitmap(new GlobalHistogramBinarizer(source));
                try {
                    Result r = new MultiFormatReader().decode(bitmap, HINTS);
                    if (r != null) {
                        previous = r.getText();
                        return previous;
                    }
                } catch (NotFoundException e) {
                    return null;
                }
            }
        }
        return null;
    }

}