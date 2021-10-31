package nsu.networks.snakes.view.windows;

import nsu.networks.snakes.controller.KeyboardController;
import nsu.networks.snakes.model.Presenter;
import nsu.networks.snakes.model.SnakesProto;
import nsu.networks.snakes.model.Configuration;
import nsu.networks.snakes.view.View;
import nsu.networks.snakes.view.panels.creating.CreatingGameListener;
import nsu.networks.snakes.view.panels.creating.CreatingGamePanel;
import nsu.networks.snakes.view.panels.creating.configuration.ConfigurationSettingsListener;
import nsu.networks.snakes.view.panels.creating.configuration.ConfigurationSettingsPanel;
import nsu.networks.snakes.view.panels.finding.FindingGameListener;
import nsu.networks.snakes.view.panels.finding.FindingGamePanel;
import nsu.networks.snakes.view.panels.finding.JoiningGameListener;
import nsu.networks.snakes.view.panels.finding.JoiningGamePanel;
import nsu.networks.snakes.view.panels.game.GamePanel;
import nsu.networks.snakes.view.panels.game.GamePanelListener;
import nsu.networks.snakes.view.panels.startMenu.StartListener;
import nsu.networks.snakes.view.panels.startMenu.StartPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

public class MainWindow extends JFrame implements View, StartListener, CreatingGameListener, FindingGameListener, JoiningGameListener, GamePanelListener, ConfigurationSettingsListener {
    private static final String NAME = "Snakes";
    private static final String MENU = "Menu";
    private final int widthWindow;
    private final int heightWindow;

    private int widthField;
    private int heightField;

    private Presenter presenter;

    private StartPanel startPanel;
    private CreatingGamePanel creatingGamePanel;
    private FindingGamePanel findingGamePanel;
    private JoiningGamePanel joiningGamePanel;
    private ConfigurationSettingsPanel configurationSettingsPanel;
    private GamePanel gamePanel;

    public MainWindow() {
        super(NAME);
        this.widthWindow = 896;
        this.heightWindow = widthWindow / 16 * 9;
        this.setFocusable(true);
        this.setResizable(false);
        startPanel = new StartPanel(this, widthWindow, heightWindow);
        this.setContentPane(startPanel);

        this.pack();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                closeTheGame();
            }
        });
    }

    private void setContentOnFrame(Container pane) {
        this.setContentPane(pane);
        this.repaint();
        this.setVisible(true);
    }

    @Override
    public void launchFindingGameModule() {
        joiningGamePanel = new JoiningGamePanel(this,widthWindow,heightWindow);
        setContentOnFrame(joiningGamePanel);
        presenter.findTheGame(findingGamePanel.name,findingGamePanel.port, findingGamePanel.playerType);
    }

    @Override
    public void joiningToGame(String gameKey) {
        if(findingGamePanel.isViewer) {
            presenter.joinTheGame(SnakesProto.NodeRole.VIEWER, gameKey);
        }
        else presenter.joinTheGame(SnakesProto.NodeRole.NORMAL, gameKey);
    }

    @Override
    public void openField(int widthField, int heightField){
        this.addKeyListener(new KeyboardController(presenter));
        this.widthField = widthField;
        this.heightField = heightField;
        gamePanel = new GamePanel(this, widthWindow, heightWindow, widthField, heightField);
        setContentOnFrame(gamePanel);
    }

    @Override
    public void closeTheGame() {
        presenter.endTheSession();
        System.exit(0);
    }

    @Override
    public void backToCreatingGameMenu() {
        setContentOnFrame(creatingGamePanel);
    }

    @Override
    public void backToStartMenu() {
        setContentOnFrame(startPanel);
    }

    @Override
    public void createNewGame() {
        creatingGamePanel = new CreatingGamePanel(this, widthWindow, heightWindow);
        setContentOnFrame(creatingGamePanel);
    }

    @Override
    public void findGames() {
        findingGamePanel = new FindingGamePanel(this,widthWindow,heightWindow);
        setContentOnFrame(findingGamePanel);
    }

    @Override
    public void attachPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void changeVisible(boolean var) {
        this.setVisible(var);
    }

    @Override
    public void updateGameView(String fieldString, List<String> scoresTable, String nodeRole) {
        gamePanel.updateGamePanel(fieldString, scoresTable, nodeRole);
    }

    @Override
    public void updateFindGameList(List<String> games) {
        joiningGamePanel.updateUserList(games);
    }

    @Override
    public void startTheGame() {
        if (configurationSettingsPanel == null) {
            openField(Configuration.width,Configuration.height);
            presenter.startTheGame(creatingGamePanel.name,
                    creatingGamePanel.port,
                    creatingGamePanel.playerType);
        } else {
            if (configurationSettingsPanel.isSaved) {
                openField(configurationSettingsPanel.widthGame,configurationSettingsPanel.heightGame);
                presenter.startTheGame(creatingGamePanel.name,
                        creatingGamePanel.port,
                        creatingGamePanel.playerType,
                        configurationSettingsPanel.widthGame,
                        configurationSettingsPanel.heightGame,
                        configurationSettingsPanel.foodStatic,
                        configurationSettingsPanel.foodPerPlayer,
                        configurationSettingsPanel.stateDelay,
                        configurationSettingsPanel.deadFoodProb,
                        configurationSettingsPanel.pingDelay,
                        configurationSettingsPanel.nodeTimeout);

            } else {
                widthField = 40;
                heightField = 30;
                gamePanel = new GamePanel(this, widthWindow, heightWindow, widthField, heightField);
                presenter.startTheGame(creatingGamePanel.name,
                        creatingGamePanel.port,
                        creatingGamePanel.playerType);
            }
        }
    }

    @Override
    public void openConfigSettings() {
        if (configurationSettingsPanel == null) {
            configurationSettingsPanel = new ConfigurationSettingsPanel(this, widthWindow, heightWindow);
        }
        setContentOnFrame(configurationSettingsPanel);
    }

    @Override
    public void changeRoleOnViewer() {
        presenter.changeRoleOnViewer();
    }

    @Override
    public void leaveTheGame() {
        presenter.leaveTheGame();
        backToStartMenu();
    }
}
