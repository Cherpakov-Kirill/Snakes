package nsu.networks.snakes.view.panels.finding;

import nsu.networks.snakes.view.ViewUtils;
import nsu.networks.snakes.view.panels.WindowPanel;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static nsu.networks.snakes.view.ViewUtils.getPart;

public class JoiningGamePanel extends WindowPanel {
    private final JoiningGameListener listener;
    private final JScrollPane scrollPane;
    private final Map<String, GameLinePanel> gamePanelsMap;
    private String chosenGame;
    private final int gameLineWidth;
    private final int gameLineHeight;
    private final int widthWindow;
    private final int heightWindow;

    public JoiningGamePanel(JoiningGameListener listener, int width, int height) {
        super("/" + "GameListForConnecting.png", width, height);
        this.listener = listener;
        this.widthWindow = width;
        this.heightWindow = height;
        this.gamePanelsMap = new HashMap<>();
        this.gameLineWidth = getPart(widthWindow, 0.6);
        this.gameLineHeight = getPart(heightWindow, 0.1);
        add(ViewUtils.initButton(getPart(width, 0.0929), getPart(height, 0.1041), getPart(width, 0.01570), getPart(height, 0.0277), e -> listener.closeTheGame()));
        add(ViewUtils.initButton(getPart(width, 0.11), getPart(height, 0.082), getPart(width, 0.009), getPart(height, 0.16), e -> listener.backToStartMenu()));
        add(ViewUtils.initButton(getPart(width, 0.093), getPart(height, 0.15), getPart(width, 0.879), getPart(height, 0.8), e -> joinTheGame()));
        scrollPane = ViewUtils.initScrollPane((int)(gameLineWidth*1.025), gameLineHeight*7, getPart(width, 0.2), getPart(height, 0.25));
        add(scrollPane);
    }

    public void joinTheGame(){
        if(chosenGame != null){
            listener.joiningToGame(chosenGame);
        }
    }

    public void chooseGame(String name) {
        if (chosenGame != null) {
            if (chosenGame.equals(name)) return;
            gamePanelsMap.get(chosenGame).makeDefaultBackground();
        }
        chosenGame = name;
    }

    public void updateUserList(List<String> list) {
        boolean isChosenAnyDialog = false;
        JPanel gamesPanel = new JPanel();
        gamesPanel.setOpaque(false);
        gamesPanel.setLayout(null);
        int number = 0;
        for (String data : list) {
            GameLinePanel gameLinePanel = new GameLinePanel(this, "GameLine.png", gameLineWidth, gameLineHeight, 0, gameLineHeight * number, data);
            if (chosenGame == null || chosenGame.equals(data)) {
                chosenGame = data;
                gameLinePanel.makeChosenBackground();
                isChosenAnyDialog = true;
            }
            gamePanelsMap.put(data, gameLinePanel);
            gamesPanel.add(gameLinePanel);
            number++;
        }
        gamesPanel.setPreferredSize(new Dimension(gameLineWidth, gameLineHeight*number));
        gamesPanel.revalidate();
        scrollPane.setViewportView(gamesPanel);
        if (!isChosenAnyDialog && !list.isEmpty()) {
            String data = list.get(0);
            chosenGame = data;
            gamePanelsMap.get(data).makeChosenBackground();
        }
    }
}
