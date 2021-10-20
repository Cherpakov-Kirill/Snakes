package nsu.networks.snakes.view.panels.game;

import javax.swing.*;
import java.awt.*;

public class CellPanel extends JPanel {
    private ImageIcon imageIcon;
    private final int cellSize;

    public CellPanel(int cellSize, ImageIcon defaultIcon) {
        setLayout(null);
        this.cellSize = cellSize;
        this.imageIcon = defaultIcon;
        setPreferredSize(new Dimension(cellSize, cellSize));
        repaint();
    }

    public void changeBackground(ImageIcon icon) {
        this.imageIcon = icon;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(imageIcon.getImage(), 0, 0, null);
        Toolkit.getDefaultToolkit().sync();
    }
}
