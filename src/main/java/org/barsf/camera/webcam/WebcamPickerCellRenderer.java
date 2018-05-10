package org.barsf.camera.webcam;

import javax.swing.*;
import java.awt.*;


public class WebcamPickerCellRenderer extends JLabel implements ListCellRenderer<Webcam> {

    private static final long serialVersionUID = 1L;

    private static final ImageIcon ICON = new ImageIcon(WebcamPickerCellRenderer.class.getResource("/org/barsf/camera/icons/camera-icon.png"));

    public WebcamPickerCellRenderer() {
        setOpaque(true);
        setHorizontalAlignment(LEFT);
        setVerticalAlignment(CENTER);
        setIcon(ICON);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends Webcam> list, Webcam webcam, int i, boolean selected, boolean focused) {

        if (selected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }

        setText(webcam.getName());
        setFont(list.getFont());

        return this;
    }
}
