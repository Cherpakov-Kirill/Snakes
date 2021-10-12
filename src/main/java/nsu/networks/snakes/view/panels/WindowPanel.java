package nsu.networks.snakes.view.panels;

import nsu.networks.snakes.view.windows.MainWindow;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;

public class WindowPanel extends JPanel {
    private ImageIcon imageIcon;
    private final int width;
    private final int height;

    public void setImageIcon(String fileName) {
        URL file = MainWindow.class.getResource(fileName);
        if (file == null) {
            BufferedImage defaultBackground = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = defaultBackground.createGraphics();

            graphics.setPaint(new Color(46, 101, 217));
            graphics.fillRect(0, 0, defaultBackground.getWidth(), defaultBackground.getHeight());

            this.imageIcon = new ImageIcon(defaultBackground);
        } else {
            ImageIcon background = new ImageIcon(file);
            Image img = background.getImage();
            Image temp = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            background = new ImageIcon(temp);
            this.imageIcon = background;
        }
        repaint();
    }

    public WindowPanel(String fileName, int width, int height) {
        setLayout(null);
        this.width = width;
        this.height = height;
        setPreferredSize(new Dimension(width, height));
        setImageIcon(fileName);
        this.setFocusable(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(imageIcon.getImage(), 0, 0, null);
    }
}
