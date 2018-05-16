package org.barsf.screen.main;

import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.barsf.screen.image.ImageType;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

public class Screen {
    private static JFrame frame = null;
    private static JPanel panel = null;
    private static JLabel label = null;
    private static JLabel info = null;
    private static JButton button = null;

    private static int imageSize = 0;

    public Screen() {
        frame = new JFrame();

        frame.setSize(Toolkit.getDefaultToolkit().getScreenSize());
        frame.setLocation(0, 0);
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(Color.WHITE);

        //   Need to Add
        label = new JLabel();
        label.setSize(800, 400);
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setForeground(Color.BLACK);

        info = new JLabel();
        info.setHorizontalAlignment(JLabel.CENTER);
        info.setForeground(Color.BLACK);


        button = new JButton("Next");
        button.addActionListener(e -> {
            try {
                display(getRandomString());
            } catch (WriterException e1) {
                e1.printStackTrace();
            }
        });
        button.setSize(50, 20);
        //------------------------------------------------
        frame.add(label, BorderLayout.CENTER);
        frame.add(info, BorderLayout.NORTH);
        //Something to set
        frame.setBackground(Color.BLACK);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        //Display the window fullscreen
        // GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(frame);
        int screenWidth = ((int) Toolkit.getDefaultToolkit().getScreenSize().width * 80 / 100);
        int screenHeight = ((int) Toolkit.getDefaultToolkit().getScreenSize().height * 80 / 100);
        imageSize = screenWidth > screenHeight ? screenHeight : screenWidth;
    }

    public void display(String text) throws WriterException {
        BufferedImage bi = QRCode.from(text)
                .withSize(imageSize, imageSize)
                .withErrorCorrection(ErrorCorrectionLevel.L)
                .withCharset("UTF-8")
                .withHint(EncodeHintType.MARGIN, 0)
                .to(ImageType.PNG)
                .genBufferedImage();
        ImageIcon ico = new ImageIcon(bi);
        label.setIcon(ico);
        info.setText(text.length() + " - " + text);
    }

    private String getRandomString() {
        String dict = "qwertyuiopasdfghjklzxcvbnm,.;'[]\\\"|=-0987654321`!@#$%&*()_+}{:?><";
        StringBuffer text = new StringBuffer();
        Random rand = new Random();
        int cnt = rand.nextInt(100 + 1) + 300;
        for (int j = 0; j < cnt; j++) {
            Random rand1 = new Random();
            int index = rand1.nextInt(dict.length() - 1 - 0 + 1) + 0;
            text.append(dict.charAt(index));
        }
        return text.toString();
    }
}
