package nsu.networks.snakes.model;

import nsu.networks.snakes.model.net.multicast.MulticastReceiver;
import nsu.networks.snakes.model.net.unicast.UnicastReceiver;
import nsu.networks.snakes.view.View;

import java.util.List;


public class Presenter implements NodeListener {
    private final View view;
    private Node node;

    public Presenter(View view) {
        this.view = view;
        this.view.attachPresenter(this);
    }

    public void launchTheGame() {
        view.changeVisible(true);
    }

    private void initNode(String name, int port, SnakesProto.PlayerType type) {
        node = new Node(this, name, port, type);
    }

    public void findTheGame(String name, int port, SnakesProto.PlayerType type) {
        initNode(name, port, type);
    }

    public void joinTheGame(SnakesProto.NodeRole nodeRole, String gameKey){
        node.changeNodeRole(nodeRole);
        node.joinTheGame(gameKey);
    }

    public void startTheGame(String name, int port, SnakesProto.PlayerType type) {
        SnakesProto.GameConfig config = Configuration.defaultConfigBuilder();
        initNode(name,port,type);
        node.setStartNodeParameters(config,1);
        node.changeNodeRole(SnakesProto.NodeRole.MASTER);
        System.out.println("Game was started");
    }

    public void startTheGame(String name, int port, SnakesProto.PlayerType type, int width, int height, int foodStatic, float foodPerPlayer, int stateDelay, float deadFoodProb, int pingDelay, int nodeTimeout) {
        SnakesProto.GameConfig config = Configuration.configBuilder(width, height, foodStatic, foodPerPlayer, stateDelay, deadFoodProb, pingDelay, nodeTimeout);
        initNode(name,port,type);
        node.setStartNodeParameters(config,1);
        node.changeNodeRole(SnakesProto.NodeRole.MASTER);
        System.out.println("Game was started");
    }

    public void openFieldWindow(int widthField, int heightField){
        view.openField(widthField,heightField);
    }

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
    public void updateField(String field) {
        view.updateField(field);
    }

    @Override
    public void updateFindGameList(List<String> games) {
        view.updateFindGameList(games);
    }
}
