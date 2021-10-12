package nsu.networks.snakes.view.panels.startMenu;

import nsu.networks.snakes.view.ViewUtils;
import nsu.networks.snakes.view.panels.WindowPanel;

import static nsu.networks.snakes.view.ViewUtils.getPart;

public class StartPanel extends WindowPanel {
    public StartPanel(StartListener listener, int width, int height) {
        super(System.getProperty("file.separator") + "Start.png", width, height);
        add(ViewUtils.initButton(getPart(width,0.29), getPart(height,0.1555), getPart(width,0.35555), getPart(height,0.373), e -> listener.createNewGame()));
        add(ViewUtils.initButton(getPart(width,0.29), getPart(height,0.1555), getPart(width,0.35555), getPart(height,0.607), e -> listener.findGames()));
        add(ViewUtils.initButton(getPart(width,0.0929), getPart(height,0.1041), getPart(width,0.01570), getPart(height,0.0277), e -> listener.closeTheGame()));
    }
}
