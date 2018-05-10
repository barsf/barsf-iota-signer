package org.barsf.camera.webcam;

import java.awt.image.BufferedImage;


public interface WebcamImageTransformer {

    BufferedImage transform(BufferedImage image);

}
