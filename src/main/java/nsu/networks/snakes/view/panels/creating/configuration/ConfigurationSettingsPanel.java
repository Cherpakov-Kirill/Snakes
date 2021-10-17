package nsu.networks.snakes.view.panels.creating.configuration;

import nsu.networks.snakes.view.ViewUtils;
import nsu.networks.snakes.view.panels.WindowPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import static nsu.networks.snakes.view.ViewUtils.getPart;

public class ConfigurationSettingsPanel extends WindowPanel {
    private final ConfigurationSettingsListener listener;
    private final int width;
    private final int height;
    private final int fontSize;
    private int settingWidth;
    private int settingHeight;
    public int widthGame;
    public int heightGame;
    public int foodStatic;
    public float foodPerPlayer;
    public int stateDelay;
    public float deadFoodProb;
    public int pingDelay;
    public int nodeTimeout;
    public boolean isSaved;

    private final JTextField widthGameField;
    private final JTextField heightGameField;
    private final JTextField foodStaticField;
    private final JTextField foodPerPlayerField;
    private final JTextField stateDelayField;
    private final JTextField deadFoodProbField;
    private final JTextField pingDelayField;
    private final JTextField nodeTimeoutField;

    private void saveConfig(){
        isSaved = true;
        listener.backToCreatingGameMenu();
    }

    private void resetConfig(){
        isSaved = false;
        widthGame = 40;
        heightGame = 30;
        foodStatic = 1;
        foodPerPlayer = 1;
        stateDelay = 400; //todo 1000
        deadFoodProb = (float)0.1;
        pingDelay = 100;
        nodeTimeout = 800;
        widthGameField.setText(String.valueOf(widthGame));
        heightGameField.setText(String.valueOf(heightGame));
        foodStaticField.setText(String.valueOf(foodStatic));
        foodPerPlayerField.setText(String.valueOf(foodPerPlayer));
        stateDelayField.setText(String.valueOf(stateDelay));
        deadFoodProbField.setText(String.valueOf(deadFoodProb));
        pingDelayField.setText(String.valueOf(pingDelay));
        nodeTimeoutField.setText(String.valueOf(nodeTimeout));
    }

    private boolean isValidValueInteger(int value, int leftLimit, int rightLimit){
        if(value<leftLimit||value>rightLimit){
            String message = "Please, type value only from interval ("+leftLimit+","+rightLimit+")";
            JOptionPane.showMessageDialog(ConfigurationSettingsPanel.this,message);
            return false;
        }
        return true;
    }

    private boolean isValidValueInteger(float value, float leftLimit, float rightLimit){
        if(value<leftLimit||value>rightLimit){
            String message = "Please, type value only from interval ("+leftLimit+","+rightLimit+")";
            JOptionPane.showMessageDialog(ConfigurationSettingsPanel.this,message);
            return false;
        }
        return true;
    }

    public ConfigurationSettingsPanel(ConfigurationSettingsListener listener, int width, int height) {
        super("/" + "ConfigurationSettings.png", width, height);
        this.listener = listener;
        this.isSaved = false;
        this.width = width;
        this.height = height;
        this.settingWidth = getPart(width, 0.08);
        this.settingHeight = getPart(width, 0.03);
        this.fontSize = getPart(width, 0.013);
        this.widthGame = 40;
        this.heightGame = 30;
        this.foodStatic = 1;
        this.foodPerPlayer = 1;
        this.stateDelay = 1000;
        this.deadFoodProb = (float)0.1;
        this.pingDelay = 100;
        this.nodeTimeout = 800;
        add(ViewUtils.initButton(getPart(width, 0.0929), getPart(height, 0.1041), getPart(width, 0.01570), getPart(height, 0.0277), e -> listener.closeTheGame()));
        add(ViewUtils.initButton(getPart(width, 0.11), getPart(height, 0.082), getPart(width, 0.009), getPart(height, 0.16), e -> listener.backToCreatingGameMenu()));
        add(ViewUtils.initButton(getPart(width, 0.35), getPart(height, 0.175), getPart(width, 0.622), getPart(height, 0.77), e -> saveConfig()));
        add(ViewUtils.initButton(getPart(width, 0.098), getPart(height, 0.086), getPart(width, 0.52), getPart(height, 0.67), e -> resetConfig()));

        widthGameField = ViewUtils.initTextField(String.valueOf(widthGame), Color.BLACK, fontSize, 3, settingWidth, settingHeight, getPart(width, 0.325), getPart(height, 0.295));
        widthGameField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                widthGameField.setText("");
            }
            @Override
            public void focusLost(FocusEvent e) {
                String parsedText = widthGameField.getText();
                if (parsedText.isEmpty()) widthGameField.setText(String.valueOf(widthGame));
                else {
                    int value = Integer.parseInt(parsedText);
                    if(value!= widthGame){
                        if(isValidValueInteger(value,10,100)) {
                            widthGame = value;
                            System.out.println("Width = " + widthGame);
                        }
                        else{
                            widthGameField.setText(String.valueOf(widthGame));
                        }
                    }
                }

            }
        });
        add(widthGameField);

        heightGameField = ViewUtils.initTextField(String.valueOf(heightGame), Color.BLACK, fontSize, 3, settingWidth, settingHeight, getPart(width, 0.325), getPart(height, 0.395));
        heightGameField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                heightGameField.setText("");
            }
            @Override
            public void focusLost(FocusEvent e) {
                String parsedText = heightGameField.getText();
                if (parsedText.isEmpty()) heightGameField.setText(String.valueOf(heightGame));
                else {
                    int value = Integer.parseInt(parsedText);
                    if(value!= heightGame){
                        if(isValidValueInteger(value,10,100)) {
                            heightGame = value;
                            System.out.println("Height = " + heightGame);
                        }
                        else{
                            heightGameField.setText(String.valueOf(heightGame));
                        }
                    }
                }
            }
        });
        add(heightGameField);

        foodStaticField = ViewUtils.initTextField(String.valueOf(foodStatic), Color.BLACK, fontSize, 3, settingWidth, settingHeight, getPart(width, 0.795), getPart(height, 0.289));
        foodStaticField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                foodStaticField.setText("");
            }
            @Override
            public void focusLost(FocusEvent e) {
                String parsedText = foodStaticField.getText();
                if (parsedText.isEmpty()) foodStaticField.setText(String.valueOf(foodStatic));
                else {
                    int value = Integer.parseInt(parsedText);
                    if(value!=foodStatic){
                        if(isValidValueInteger(value,0,100)) {
                            foodStatic = value;
                            System.out.println("Food static = " + foodStatic);
                        }
                        else{
                            foodStaticField.setText(String.valueOf(foodStatic));
                        }
                    }
                }
            }
        });
        add(foodStaticField);

        foodPerPlayerField = ViewUtils.initTextField(String.valueOf(foodPerPlayer), Color.BLACK, fontSize, 3, settingWidth, settingHeight, getPart(width, 0.795), getPart(height, 0.389));
        foodPerPlayerField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                foodPerPlayerField.setText("");
            }
            @Override
            public void focusLost(FocusEvent e) {
                String parsedText = foodPerPlayerField.getText();
                if (parsedText.isEmpty()) foodPerPlayerField.setText(String.valueOf(foodPerPlayer));
                else {
                    float value = Float.parseFloat(parsedText);
                    if(value!=foodPerPlayer){
                        if(isValidValueInteger(value,0,100)) {
                            foodPerPlayer = value;
                            System.out.println("Food per player = " + foodPerPlayer);
                        }
                        else{
                            foodPerPlayerField.setText(String.valueOf(foodPerPlayer));
                        }
                    }
                }
            }
        });
        add(foodPerPlayerField);

        stateDelayField = ViewUtils.initTextField(String.valueOf(stateDelay), Color.BLACK, fontSize, 4, settingWidth, settingHeight, getPart(width, 0.325), getPart(height, 0.726));
        stateDelayField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                stateDelayField.setText("");
            }
            @Override
            public void focusLost(FocusEvent e) {
                String parsedText = stateDelayField.getText();
                if (parsedText.isEmpty()) stateDelayField.setText(String.valueOf(stateDelay));
                else {
                    int value = Integer.parseInt(parsedText);
                    if(value!=stateDelay){
                        if(isValidValueInteger(value,1,10000)) {
                            stateDelay = value;
                            System.out.println("State delay = " + stateDelay);
                        }
                        else{
                            stateDelayField.setText(String.valueOf(stateDelay));
                        }
                    }
                }
            }
        });
        add(stateDelayField);

        deadFoodProbField = ViewUtils.initTextField(String.valueOf(deadFoodProb), Color.BLACK, fontSize, 3, settingWidth, settingHeight, getPart(width, 0.795), getPart(height, 0.546));
        deadFoodProbField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                deadFoodProbField.setText("");
            }
            @Override
            public void focusLost(FocusEvent e) {
                String parsedText = deadFoodProbField.getText();
                if (parsedText.isEmpty()) deadFoodProbField.setText(String.valueOf(deadFoodProb));
                else {
                    float value = Float.parseFloat(parsedText);
                    if(value!=deadFoodProb){
                        if(isValidValueInteger(value,0,1)) {
                            deadFoodProb = value;
                            System.out.println("Food per player = " + deadFoodProb);
                        }
                        else{
                            deadFoodProbField.setText(String.valueOf(deadFoodProb));
                        }
                    }
                }
            }
        });
        add(deadFoodProbField);

        pingDelayField = ViewUtils.initTextField(String.valueOf(pingDelay), Color.BLACK, fontSize, 4, settingWidth, settingHeight, getPart(width, 0.325), getPart(height, 0.852));
        pingDelayField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                pingDelayField.setText("");
            }
            @Override
            public void focusLost(FocusEvent e) {
                String parsedText = pingDelayField.getText();
                if (parsedText.isEmpty()) pingDelayField.setText(String.valueOf(pingDelay));
                else {
                    int value = Integer.parseInt(parsedText);
                    if(value!=pingDelay){
                        if(isValidValueInteger(value,1,10000)) {
                            pingDelay = value;
                            System.out.println("Ping delay = " + pingDelay);
                        }
                        else{
                            pingDelayField.setText(String.valueOf(pingDelay));
                        }
                    }
                }
            }
        });
        add(pingDelayField);

        nodeTimeoutField = ViewUtils.initTextField(String.valueOf(nodeTimeout), Color.BLACK, fontSize, 4, settingWidth, settingHeight, getPart(width, 0.325), getPart(height, 0.61));
        nodeTimeoutField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                nodeTimeoutField.setText("");
            }
            @Override
            public void focusLost(FocusEvent e) {
                String parsedText = nodeTimeoutField.getText();
                if (parsedText.isEmpty()) nodeTimeoutField.setText(String.valueOf(nodeTimeout));
                else {
                    int value = Integer.parseInt(parsedText);
                    if(value!=nodeTimeout){
                        if(isValidValueInteger(value,1,10000)) {
                            nodeTimeout = value;
                            System.out.println("Node timeout = " + nodeTimeout);
                        }
                        else{
                            nodeTimeoutField.setText(String.valueOf(nodeTimeout));
                        }
                    }
                }
            }
        });
        add(nodeTimeoutField);
    }
}
