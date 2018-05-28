package org.barsf.screen;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;

public class Screen {

    public static final HashMap<EncodeHintType, Object> HINTS = new HashMap<>();
    private static final MatrixToImageConfig DEFAULT_MATRIX_TO_IMAGE_CONFIG = new MatrixToImageConfig();
    private static final QRCodeWriter QR_CODE_WRITER = new QRCodeWriter();

    static {
        HINTS.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        HINTS.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
        HINTS.put(EncodeHintType.MARGIN, 0);
    }

    private JFrame frame;
    private JLabel imageLabel;
    private JLabel infoLabel;
    private int imageSize;

    public Screen() {
        frame = new JFrame();

        frame.setSize(Toolkit.getDefaultToolkit().getScreenSize());
        frame.setLocation(2, 2);
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(Color.WHITE);

        imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(JLabel.CENTER);

        infoLabel = new JLabel();
        infoLabel.setHorizontalAlignment(JLabel.CENTER);
        infoLabel.setForeground(Color.BLACK);

        //------------------------------------------------
        frame.add(imageLabel, BorderLayout.CENTER);
        frame.add(infoLabel, BorderLayout.NORTH);
        //Something to set
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
        int screenWidth = Toolkit.getDefaultToolkit().getScreenSize().width * 80 / 100;
        int screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height * 80 / 100;
        imageSize = screenWidth > screenHeight ? screenHeight : screenWidth;
    }

    public void display(String text) throws WriterException {
        BitMatrix qrCode = QR_CODE_WRITER.encode(text, BarcodeFormat.QR_CODE, imageSize, imageSize, HINTS);
        BufferedImage bi = MatrixToImageWriter.toBufferedImage(qrCode, DEFAULT_MATRIX_TO_IMAGE_CONFIG);
        ImageIcon ico = new ImageIcon(bi);
        imageLabel.setIcon(ico);
        imageLabel.repaint();
        infoLabel.setText(text.length() + " - " + text);
    }

    public void toFront() {
        EventQueue.invokeLater(() -> {
            frame.toFront();
            frame.repaint();
        });
    }
}
