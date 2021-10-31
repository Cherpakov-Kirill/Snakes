package nsu.networks.snakes.view.panels.game;

import nsu.networks.snakes.view.ViewUtils;
import nsu.networks.snakes.view.panels.WindowPanel;

import javax.swing.*;
import java.awt.*;

public class ScoreLinePanel extends WindowPanel {
    private final String filename;

    public ScoreLinePanel(String filename, int width, int height, int posX, int posY, String data){
        super("/" + filename,width,height);
        this.filename = filename;
        Dimension size = this.getPreferredSize();
        this.setBounds(posX, posY, size.width, size.height);
        JLabel name = ViewUtils.initLabel(data, ViewUtils.getPart(height,0.32), width, ViewUtils.getPart(height,0.5) , ViewUtils.getPart(height,0.2), ViewUtils.getPart(height,0.25));
        add(name);
    }
}
