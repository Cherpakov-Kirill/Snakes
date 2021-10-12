package nsu.networks.snakes.view.panels.creating;

import nsu.networks.snakes.model.SnakesProto;
import nsu.networks.snakes.view.ViewUtils;
import nsu.networks.snakes.view.panels.WindowPanel;
import nsu.networks.snakes.view.windows.MainWindow;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.image.BufferedImage;
import java.net.URL;

import static nsu.networks.snakes.view.ViewUtils.getPart;

public class CreatingGamePanel extends WindowPanel {
    private static final String fileSeparator = System.getProperty("file.separator");
    private CreatingGameListener listener;
    private ImageIcon empty;
    private ImageIcon tick;
    private int playerTypeWidth;
    private int playerTypeHeight;
    private final JButton humanType;
    private final JButton computerType;
    private final int fontSize;
    private final int columns;
    public SnakesProto.PlayerType playerType;
    public int port;
    public String name;

    public CreatingGamePanel(CreatingGameListener listener, int width, int height) {
        super(fileSeparator + "CreatingNewGame.png", width, height);
        this.listener = listener;
        this.port = 1024 + (int) (Math.random() * 48126);
        this.name = "Player";
        this.fontSize = getPart(width, 0.013);
        this.columns = 15;
        add(ViewUtils.initButton(getPart(width, 0.2539), getPart(height, 0.146), getPart(width, 0.64), getPart(height, 0.7343), e -> listener.startTheGame()));
        add(ViewUtils.initButton(getPart(width, 0.315), getPart(height, 0.12), getPart(width, 0.186), getPart(height, 0.705), e -> listener.openConfigSettings()));
        add(ViewUtils.initButton(getPart(width, 0.0929), getPart(height, 0.1041), getPart(width, 0.01570), getPart(height, 0.0277), e -> listener.closeTheGame()));
        add(ViewUtils.initButton(getPart(width, 0.11), getPart(height, 0.082), getPart(width, 0.009), getPart(height, 0.16), e -> listener.backToStartMenu()));

        JTextField nameField = ViewUtils.initTextField("Player", Color.WHITE, fontSize, columns, getPart(width, 0.11), getPart(height, 0.05), getPart(width, 0.19), getPart(height, 0.32));
        nameField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                nameField.setText("");
            }

            @Override
            public void focusLost(FocusEvent e) {
                String parsedText = nameField.getText();
                if (parsedText.isEmpty()) {
                    name = "Player";
                } else name = (nameField.getText());
            }
        });
        add(nameField);

        JTextField portField = ViewUtils.initTextField(String.valueOf(port), Color.WHITE, fontSize, columns, getPart(width, 0.11), getPart(height, 0.05), getPart(width, 0.19), getPart(height, 0.53));
        portField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                portField.setText("");
            }

            @Override
            public void focusLost(FocusEvent e) {
                int parsedInt = Integer.parseInt(portField.getText());
                if (parsedInt < 1024 || parsedInt > 49151) {
                    portField.setText("Wrong port " + parsedInt);
                    JOptionPane.showMessageDialog(CreatingGamePanel.this,
                            "Please, type port only from interval (1024, 49151)");
                    portField.setText(String.valueOf(port));
                } else {
                    port = parsedInt;
                }
            }
        });
        add(portField);

        this.playerTypeWidth = getPart(width, 0.03125);
        this.playerTypeHeight = getPart(height, 0.0555);
        this.playerType = SnakesProto.PlayerType.HUMAN;
        this.empty = getImageButtonIcon(fileSeparator + "Empty.png", Color.GREEN);
        this.tick = getImageButtonIcon(fileSeparator + "Tick.png", Color.BLUE);
        this.humanType = initButtonForPlayerType(tick, getPart(width, 0.645), getPart(height, 0.377));
        this.computerType = initButtonForPlayerType(empty, getPart(width, 0.645), getPart(height, 0.435));
        add(humanType);
        add(computerType);
    }

    private void changeChosenBox(JButton button) {
        if (button == humanType) {
            playerType = SnakesProto.PlayerType.HUMAN;
            humanType.setIcon(tick);
            computerType.setIcon(empty);
        } else {
            if (button == computerType) {
                playerType = SnakesProto.PlayerType.ROBOT;
                humanType.setIcon(empty);
                computerType.setIcon(tick);
            }
        }
    }

    public JButton initButtonForPlayerType(ImageIcon icon, int posX, int posY) {
        JButton button = new JButton();
        button.setPreferredSize(new Dimension(playerTypeWidth, playerTypeHeight));
        Dimension settingShipsSize = button.getPreferredSize();
        button.setBounds(posX, posY, settingShipsSize.width, settingShipsSize.height);

        button.setIcon(icon);

        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.addActionListener(e -> changeChosenBox(button));
        return button;
    }

    private ImageIcon getImageButtonIcon(String fileDirectory, Color colorForButton) {
        URL file = MainWindow.class.getResource(fileDirectory);
        if (file == null) {
            BufferedImage defaultBackground = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = defaultBackground.createGraphics();
            graphics.setPaint(colorForButton);
            graphics.fillRect(0, 0, defaultBackground.getWidth(), defaultBackground.getHeight());
            return new ImageIcon(defaultBackground);
        } else {
            ImageIcon icon = new ImageIcon(file);
            Image img = icon.getImage();
            Image temp = img.getScaledInstance(playerTypeWidth, playerTypeHeight, Image.SCALE_DEFAULT);
            return new ImageIcon(temp);
        }
    }
}
