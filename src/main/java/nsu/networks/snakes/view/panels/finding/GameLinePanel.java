package nsu.networks.snakes.view.panels.finding;

import nsu.networks.snakes.view.ViewUtils;
import nsu.networks.snakes.view.panels.WindowPanel;

import javax.swing.*;
import java.awt.*;

public class GameLinePanel extends WindowPanel {
    private final JoiningGamePanel joiningGamePanel;
    private final String filename;

    public GameLinePanel(JoiningGamePanel joiningGamePanel, String filename, int width, int height, int posX, int posY, String data){
        super(System.getProperty("file.separator") + filename,width,height);
        this.filename = filename;
        this.joiningGamePanel = joiningGamePanel;
        Dimension size = this.getPreferredSize();
        this.setBounds(posX, posY, size.width, size.height);
        JLabel name = ViewUtils.initLabel(data, 16, 400, 50, 70, 5);
        add(name);
        JButton button = ViewUtils.initButton(width, height, 0, 0, e -> {
            makeChosenBackground();
            joiningGamePanel.chooseGame(data);
        });
        add(button);
    }

    public void makeChosenBackground() {
        this.setImageIcon(System.getProperty("file.separator") + filename.substring(0, filename.indexOf('.')) + "Chosen.png");
    }

    public void makeDefaultBackground() {
        this.setImageIcon(System.getProperty("file.separator") + filename);
    }
}
