import com.aspose.barcode.examples.ApplyALicense;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        BufferedImage bufferedImage = ImageIO.read(new File("C:\\Users\\chuti\\Desktop\\screenshot2.png"));
        LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

        try {
            Result result = new MultiFormatReader().decode(bitmap);
            System.out.println(result);
        } catch (NotFoundException e) {
            System.out.println("There is no QR code in the image");
        }
    }

}
