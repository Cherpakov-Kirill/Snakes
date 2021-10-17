package nsu.networks.snakes.view.panels.game;

import nsu.networks.snakes.view.panels.WindowPanel;

import static nsu.networks.snakes.view.ViewUtils.getPart;

public class FieldPanel extends WindowPanel {
    private final int width;
    private final int height;
    private final int fieldWidth;
    private final int fieldHeight;
    private final FieldCellsPanel fieldCellsPanel;

    public FieldPanel(int widthPanel, int heightPanel, int widthField, int heightField) {
        super("/" + "Field.png", widthPanel, heightPanel);
        this.width = widthPanel;
        this.height = heightPanel;
        this.fieldWidth = widthField;
        this.fieldHeight = heightField;
        this.setLayout(null);
        int cellSize = (int)(widthPanel*0.6/fieldWidth);
        if(cellSize*fieldHeight > (int)(widthPanel*0.9)){
            cellSize = (int)(widthPanel*0.9/fieldHeight);
        }
        this.fieldCellsPanel = new FieldCellsPanel(cellSize,fieldWidth,fieldHeight);
        this.add(fieldCellsPanel);
    }
    public void updateField(String field) {
        fieldCellsPanel.updateField(field);
    }
}
