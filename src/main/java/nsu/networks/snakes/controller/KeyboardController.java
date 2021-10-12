package nsu.networks.snakes.controller;

import nsu.networks.snakes.model.Presenter;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class KeyboardController extends KeyAdapter {
    private final Presenter presenter;

    public KeyboardController(Presenter presenter){
        this.presenter = presenter;
    }

    public void keyReleased(KeyEvent event) {
       switch (event.getKeyCode()){
            case KeyEvent.VK_W ->{
                presenter.makeUpMove();
            }
            case KeyEvent.VK_A ->{
                presenter.makeLeftMove();
            }
            case KeyEvent.VK_S ->{
                presenter.makeDownMove();
            }
            case KeyEvent.VK_D ->{
                presenter.makeRightMove();
            }
        }

    }
}
