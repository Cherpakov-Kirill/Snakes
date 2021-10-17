package nsu.networks.snakes.view.panels.game;

import nsu.networks.snakes.view.windows.MainWindow;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;

import static nsu.networks.snakes.view.ViewUtils.getPart;

public class FieldCellsPanel extends JPanel{
    public ImageIcon aliveOtherSnake;
    public ImageIcon aliveNodeSnake;
    public ImageIcon food;
    public ImageIcon empty;
    private final int width;
    private final int height;
    private final int fieldWidth;
    private final int fieldHeight;
    private final int cellSize;
    private final JButton[][] cells;

    public FieldCellsPanel(int cellSize, int widthField, int heightField){
        this.width = cellSize*widthField;
        this.height = cellSize*heightField;
        this.fieldWidth = widthField;
        this.fieldHeight = heightField;
        this.cellSize = cellSize;

        setLayout(new GridLayout(heightField, widthField));
        this.setFocusable(true);
        setPreferredSize(new Dimension(width, height));
        this.setBounds(getPart(width,0.05), getPart(width,0.05), width, height);



        this.aliveOtherSnake = getImageButtonIcon("/" + "AliveOtherSnake.png", Color.GRAY);
        this.aliveNodeSnake = getImageButtonIcon("/" + "AliveNodeSnake.png", Color.GREEN);
        this.food = getImageButtonIcon("/" + "Food.png", Color.BLUE);
        this.empty = getImageButtonIcon("/" + "EmptyField.png", Color.WHITE);

        cells = new JButton[fieldHeight][fieldWidth];
        for (int y = 0; y < fieldHeight; y++) {
            for (int x = 0; x < fieldWidth; x++) {
                cells[y][x] = initButtonForField(empty);
                this.add(cells[y][x]);
            }
        }
        updateField("-".repeat(fieldHeight * fieldWidth));
    }



    public JButton initButtonForField(ImageIcon icon) {
        JButton button = new JButton();
        button.setPreferredSize(new Dimension(cellSize, cellSize));
        button.setIcon(icon);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        return button;
    }

    public void updateField(String field) {
        for (int y = 0; y < fieldHeight; y++) {
            for (int x = 0; x < fieldWidth; x++) {
                switch (field.charAt(y * fieldWidth + x)) {
                    case '-', '.' -> cells[y][x].setIcon(empty);
                    case '#' -> cells[y][x].setIcon(aliveOtherSnake);
                    case '&' -> cells[y][x].setIcon(aliveNodeSnake);
                    case '*' -> cells[y][x].setIcon(food);
                }
            }
        }
    }

    private ImageIcon getImageButtonIcon(String fileDirectory, Color colorForButton) {
        URL file = MainWindow.class.getResource(fileDirectory);
        if (file == null) {
            BufferedImage defaultBackground = new BufferedImage(cellSize, cellSize, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = defaultBackground.createGraphics();
            graphics.setPaint(colorForButton);
            graphics.fillRect(0, 0, defaultBackground.getWidth(), defaultBackground.getHeight());
            return new ImageIcon(defaultBackground);
        } else {
            ImageIcon background = new ImageIcon(file);
            Image img = background.getImage();
            Image temp = img.getScaledInstance(cellSize, cellSize, Image.SCALE_SMOOTH);
            background = new ImageIcon(temp);
            return background;
        }
    }
}

