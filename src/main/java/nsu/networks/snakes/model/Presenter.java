package nsu.networks.snakes.model;

import nsu.networks.snakes.model.node.Configuration;
import nsu.networks.snakes.model.node.Node;
import nsu.networks.snakes.model.node.NodeListener;
import nsu.networks.snakes.view.View;

import java.util.List;


public class Presenter implements NodeListener {
    private final View view;
    private Node node;

    public Presenter(View view) {
        this.view = view;
        this.view.attachPresenter(this);
    }

    //Launch Use Interface from psv main()
    public void launchUI() {
        view.changeVisible(true);
    }

    //Find a game in LAN
    public void findTheGame(String name, int port, SnakesProto.PlayerType type) {
        node = new Node(this, name, port, type);
    }

    //Join the game in LAN by gameKey from gamesList
    public void joinTheGame(SnakesProto.NodeRole nodeRole, String gameKey){
        node.joinTheGame(gameKey, nodeRole);
    }

    //Create new game
    private void createNewGame(SnakesProto.GameConfig config, String name, int port, SnakesProto.PlayerType type) {
        node = new Node(this, name, port, type);
        node.createNewGame(config,1);
    }

    public void startTheGame(String name, int port, SnakesProto.PlayerType type) {
        SnakesProto.GameConfig config = Configuration.defaultConfigBuilder();
        createNewGame(config,name,port,type);
    }

    public void startTheGame(String name, int port, SnakesProto.PlayerType type, int width, int height, int foodStatic, float foodPerPlayer, int stateDelay, float deadFoodProb, int pingDelay, int nodeTimeout) {
        SnakesProto.GameConfig config = Configuration.configBuilder(width, height, foodStatic, foodPerPlayer, stateDelay, deadFoodProb, pingDelay, nodeTimeout);
        createNewGame(config,name,port,type);
    }


    //Keyboard action listener
    public void makeRightMove() {
        node.setKeyboardAction(SnakesProto.Direction.RIGHT);
    }

    public void makeLeftMove() {
        node.setKeyboardAction(SnakesProto.Direction.LEFT);
    }

    public void makeUpMove() {
        node.setKeyboardAction(SnakesProto.Direction.UP);
    }

    public void makeDownMove() {
        node.setKeyboardAction(SnakesProto.Direction.DOWN);
    }

    public void endTheSession() {
        //TODO: make node closing
        /*if (client != null) {
            client.shutdownWorking();
            client = null;
        }*/
    }

    @Override
    public void openFieldWindow(int widthField, int heightField){
        view.openField(widthField,heightField);
    }

    @Override
    public void updateField(String field) {
        view.updateField(field);
    }

    @Override
    public void updateFindGameList(List<String> games) {
        view.updateFindGameList(games);
    }
}
