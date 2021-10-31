package nsu.networks.snakes.view.panels.game;

import nsu.networks.snakes.view.windows.MainWindow;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;

import static nsu.networks.snakes.view.ViewUtils.getPart;

public class FieldCellsPanel extends JPanel {
    public ImageIcon aliveOtherSnake;
    public ImageIcon aliveNodeSnake;
    public ImageIcon nearSnake;
    public ImageIcon food;
    public ImageIcon empty;
    private final int width;
    private final int height;
    private final int fieldWidth;
    private final int fieldHeight;
    private final int cellSize;
    private final CellPanel[][] cells;
    private final int cellsNum;

    public FieldCellsPanel(int cellSize, int widthField, int heightField) {
        this.width = cellSize * widthField;
        this.height = cellSize * heightField;
        this.fieldWidth = widthField;
        this.fieldHeight = heightField;
        this.cellSize = cellSize;

        setLayout(new GridLayout(heightField, widthField));
        this.setFocusable(true);
        setPreferredSize(new Dimension(width, height));
        this.setBounds(getPart(width, 0.05), getPart(width, 0.05), width, height);


        this.aliveOtherSnake = getImageIcon("/" + "AliveOtherSnake.png", Color.GRAY);
        this.aliveNodeSnake = getImageIcon("/" + "AliveNodeSnake.png", Color.GREEN);
        this.nearSnake = getImageIcon("/" + "NearSnake.png", Color.YELLOW);
        this.food = getImageIcon("/" + "Food.png", Color.BLUE);
        this.empty = getImageIcon("/" + "EmptyField.png", Color.WHITE);

        cells = new CellPanel[fieldHeight][fieldWidth];
        for (int y = 0; y < fieldHeight; y++) {
            for (int x = 0; x < fieldWidth; x++) {
                cells[y][x] = new CellPanel(cellSize, empty);
                this.add(cells[y][x]);
            }
        }
        String  fieldString = "-".repeat(fieldHeight * fieldWidth);
        cellsNum = widthField * heightField;
        updateField(fieldString);
    }

    public void updateField(String newFieldString) {
        for (int i = 0; i < cellsNum; i++) {
            char newSym = newFieldString.charAt(i);
                int y = i / fieldWidth;
                int x = i % fieldWidth;
                switch (newSym) {
                    case '-' -> cells[y][x].changeBackground(empty);
                    case '.' -> cells[y][x].changeBackground(empty);
                    case '#' -> cells[y][x].changeBackground(aliveOtherSnake);
                    case '&' -> cells[y][x].changeBackground(aliveNodeSnake);
                    case '*' -> cells[y][x].changeBackground(food);
                }
        }
    }

    private ImageIcon getImageIcon(String fileDirectory, Color colorForButton) {
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

