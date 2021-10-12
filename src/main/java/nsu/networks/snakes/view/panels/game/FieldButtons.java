package nsu.networks.snakes.view.panels.game;

import nsu.networks.snakes.view.windows.MainWindow;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;

public class FieldButtons {
    private static final String fileSeparator = System.getProperty("file.separator");
    public static ImageIcon alive = FieldButtons.getImageButtonIcon(fileSeparator + "Alive.png", Color.GREEN);
    public static ImageIcon food = FieldButtons.getImageButtonIcon(fileSeparator + "Food.png", Color.BLUE);
    public static ImageIcon empty = FieldButtons.getImageButtonIcon(fileSeparator + "Empty.png", Color.WHITE);

    public static JButton initButtonForField(ImageIcon icon) {
        JButton button = new JButton();
        button.setPreferredSize(new Dimension(20, 20));
        button.setIcon(icon);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        return button;
    }

    public static void updateField(int fieldWidth, int fieldHeight, String field, JButton[][] matrix) {
        for (int y = 0; y < fieldHeight; y++) {
            for (int x = 0; x < fieldWidth; x++) {
                switch (field.charAt(y * fieldWidth + x)) {
                    case '-', '.' -> matrix[y][x].setIcon(FieldButtons.empty);
                    case '#' -> matrix[y][x].setIcon(FieldButtons.alive);
                    case '*' -> matrix[y][x].setIcon(FieldButtons.food);
                }
            }
        }
    }

    private static ImageIcon getImageButtonIcon(String fileDirectory, Color colorForButton) {
        URL file = MainWindow.class.getResource(fileDirectory);
        if (file == null) {
            BufferedImage defaultBackground = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = defaultBackground.createGraphics();
            graphics.setPaint(colorForButton);
            graphics.fillRect(0, 0, defaultBackground.getWidth(), defaultBackground.getHeight());
            return new ImageIcon(defaultBackground);
        } else {
            return new ImageIcon(file);
        }
    }
}

