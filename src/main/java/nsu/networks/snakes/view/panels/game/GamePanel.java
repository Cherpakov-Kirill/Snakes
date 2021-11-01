package nsu.networks.snakes.view.panels.game;

import nsu.networks.snakes.model.players.FieldPoint;
import nsu.networks.snakes.view.ViewUtils;
import nsu.networks.snakes.view.panels.WindowPanel;

import javax.swing.*;
import java.awt.*;
import java.util.List;

import static nsu.networks.snakes.view.ViewUtils.getPart;

public class GamePanel extends WindowPanel {
    private final GamePanelListener listener;
    private final int width;
    private final int height;
    private final int fieldWidth;
    private final int fieldHeight;
    private final JScrollPane scrollPane;
    private final int scoreLineWidth;
    private final int scoreLineHeight;
    private String nodeRole;
    private JLabel nodeRoleLabel;
    private JButton changeRoleOnViewer;
    private JButton leaveTheGame;
    private boolean changeRoleOnScreen;
    private final FieldCellsPanel fieldCellsPanel;

    public GamePanel(GamePanelListener listener, int widthPanel, int heightPanel, int widthField, int heightField) {
        super("/" + "Field.png", widthPanel, heightPanel);
        this.listener = listener;
        this.width = widthPanel;
        this.height = heightPanel;
        this.fieldWidth = widthField;
        this.fieldHeight = heightField;
        this.setLayout(null);
        this.scoreLineWidth = getPart(width, 0.2);
        this.scoreLineHeight = getPart(height, 0.1);
        int cellSize = (int) (width * 0.6 / fieldWidth);
        if (cellSize * fieldHeight > (int) (height * 0.9)) {
            cellSize = (int) (height * 0.9 / fieldHeight);
        }
        this.fieldCellsPanel = new FieldCellsPanel(cellSize, fieldWidth, fieldHeight);
        this.add(fieldCellsPanel);
        this.nodeRole = "";
        this.nodeRoleLabel = ViewUtils.initLabel(nodeRole, ViewUtils.getPart(height, 0.018), ViewUtils.getPart(width, 0.1), ViewUtils.getPart(height, 0.05), ViewUtils.getPart(width, 0.95), ViewUtils.getPart(height, 0.0001));
        add(nodeRoleLabel);
        int buttonWidth = getPart(width,0.19);
        int buttonHeight = getPart(height,0.1);
        this.changeRoleOnViewer = ViewUtils.initButton(buttonWidth, buttonHeight, getPart(width,0.8), getPart(height,0.75), e -> listener.changeRoleOnViewer());
        this.leaveTheGame = ViewUtils.initButton(buttonWidth, buttonHeight, getPart(width,0.797), getPart(height,0.885), e -> listener.leaveTheGame());
        add(leaveTheGame);
        this.changeRoleOnViewer.setIcon(ViewUtils.getImageButtonIcon("/BecomeAViewer.png", Color.GREEN,buttonWidth,buttonHeight));
        this.changeRoleOnScreen = false;
        scrollPane = ViewUtils.initScrollPane((int) (scoreLineWidth * 1.025), scoreLineHeight * 4, getPart(width, 0.7), getPart(height, 0.10));
        add(scrollPane);
    }

    private void updateScoresTable(List<String> scoresTable) {
        JPanel gamesPanel = new JPanel();
        gamesPanel.setOpaque(false);
        gamesPanel.setLayout(null);
        int number = 0;
        for (String data : scoresTable) {
            ScoreLinePanel scoreLinePanel = new ScoreLinePanel("GameLine.png", scoreLineWidth, scoreLineHeight, 0, scoreLineHeight * number, data);
            gamesPanel.add(scoreLinePanel);
            number++;
        }

        gamesPanel.setPreferredSize(new Dimension(scoreLineWidth, scoreLineHeight * number));
        gamesPanel.revalidate();
        scrollPane.setViewportView(gamesPanel);
    }

    private void updateRole(String nodeRole) {
        nodeRoleLabel.setText(nodeRole);
        if (!nodeRole.equals("VIEWER")) {
            if(!changeRoleOnScreen) {
                add(changeRoleOnViewer);
                changeRoleOnScreen = true;
            }
        } else if(changeRoleOnScreen) {
            remove(changeRoleOnViewer);
            changeRoleOnScreen = false;
        }
        repaint();
    }

    public void updateGamePanel(List<FieldPoint> field, List<String> scoresTable, String nodeRole) {
        if(!this.nodeRole.equals(nodeRole)){
            this.nodeRole = nodeRole;
            updateRole(nodeRole);
        }
        fieldCellsPanel.updateField(field);
        updateScoresTable(scoresTable);
    }
}
