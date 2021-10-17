package nsu.networks.snakes.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class ViewUtils {
    public static int getPart(int param, double part){
        return (int)(((double)param)*part);
    }

    public static JButton initButton(int width, int height, int posX, int posY, ActionListener listener) {
        JButton button = new JButton();
        button.setPreferredSize(new Dimension(width, height));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(true);///
        button.setOpaque(false);
        button.addActionListener(listener);
        Dimension startSize = button.getPreferredSize();
        button.setBounds(posX, posY, startSize.width, startSize.height);
        return button;
    }

    public static JLabel initLabel(String text, int fontSize, int width, int height, int posX, int posY) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Roboto", Font.BOLD, fontSize));
        label.setForeground(Color.WHITE);
        label.setPreferredSize(new Dimension(width, height));
        Dimension textSize = label.getPreferredSize();
        label.setBounds(posX, posY, textSize.width, textSize.height);
        return label;
    }

    public static JScrollPane initScrollPane(int width, int height, int posX, int posY) {
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.getViewport().setOpaque(false);
        //scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.setPreferredSize(new Dimension(width, height));
        Dimension scrollPaneSize = scrollPane.getPreferredSize();
        scrollPane.setBounds(posX, posY, scrollPaneSize.width, scrollPaneSize.height);
        return scrollPane;
    }

    public static JTextField initTextField(String defaultText, Color color, int fontSize, int columns, int width, int height, int posX, int posY){
        JTextField textField = new JTextField(defaultText, columns);
        textField.setFont(new Font("Roboto", Font.BOLD, fontSize));
        textField.setForeground(color);
        textField.setOpaque(false);
        textField.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        textField.setPreferredSize(new Dimension(width, height));
        Dimension textSize = textField.getPreferredSize();
        textField.setBounds(posX, posY, textSize.width, textSize.height);
        return textField;
    }
}
