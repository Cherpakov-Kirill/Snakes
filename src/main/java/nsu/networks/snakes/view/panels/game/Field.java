package nsu.networks.snakes.view.panels.game;

import nsu.networks.snakes.view.panels.WindowPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class Field extends WindowPanel {
    protected final int fieldWidth;
    protected final int fieldHeight;
    private final JButton[][] cells;
    private final StringBuffer field;

    private static final String fileSeparator = System.getProperty("file.separator");
    public Field(int widthPanel, int heightPanel, int widthField, int heightField) {
        super(fileSeparator + "Field.png", widthPanel, heightPanel);
        this.fieldWidth = widthField;
        this.fieldHeight = heightField;


        JPanel fieldCellsPanel = new JPanel();
        fieldCellsPanel.setLayout(new GridLayout(fieldHeight, fieldWidth));
        cells = new JButton[fieldHeight][fieldWidth];
        for (int y = 0; y < fieldHeight; y++) {
            for (int x = 0; x < fieldWidth; x++) {
                cells[y][x] = FieldButtons.initButtonForField(FieldButtons.empty);
                fieldCellsPanel.add(cells[y][x]);
            }
        }
        this.setLayout(null);
        Dimension fieldSize = fieldCellsPanel.getPreferredSize();
        fieldCellsPanel.setBounds(20, 20, fieldSize.width, fieldSize.height);
        this.add(fieldCellsPanel);
        field = new StringBuffer("-".repeat(fieldHeight * fieldWidth));
        updateField(field.toString());
    }
    public void updateField(String field) {
        FieldButtons.updateField(fieldWidth, fieldHeight, field, cells);
    }
}
