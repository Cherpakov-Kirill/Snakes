package nsu.networks.snakes.view.panels.finding;

import nsu.networks.snakes.view.ViewUtils;
import nsu.networks.snakes.view.panels.WindowPanel;

import javax.swing.*;
import java.awt.*;

public class GameLinePanel extends WindowPanel {
    private final JoiningGamePanel joiningGamePanel;
    private final String filename;

    public GameLinePanel(JoiningGamePanel joiningGamePanel, String filename, int width, int height, int posX, int posY, String data){
        super("/" + filename, width, height);
        this.filename = filename;
        this.joiningGamePanel = joiningGamePanel;
        Dimension size = this.getPreferredSize();
        this.setBounds(posX, posY, size.width, size.height);
        JLabel name = ViewUtils.initLabel(data, ViewUtils.getPart(height,0.32), ViewUtils.getPart(width,0.7), ViewUtils.getPart(height,0.5), ViewUtils.getPart(width,0.1), ViewUtils.getPart(height,0.25));
        add(name);
        JButton button = ViewUtils.initButton(width, height, 0, 0, e -> {
            makeChosenBackground();
            joiningGamePanel.chooseGame(data);
        });
        add(button);
    }

    public void makeChosenBackground() {
        this.setImageIcon("/" + filename.substring(0, filename.indexOf('.')) + "Chosen.png");
    }

    public void makeDefaultBackground() {
        this.setImageIcon("/" + filename);
    }

}
