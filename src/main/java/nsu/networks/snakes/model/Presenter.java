package nsu.networks.snakes.model;

import nsu.networks.snakes.model.players.FieldPoint;
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
        node = new Node(this, name, port, SnakesProto.PlayerType.HUMAN); //ROBOT is not available in current version of game
    }

    //Join the game in LAN by gameKey from gamesList
    public void joinTheGame(SnakesProto.NodeRole nodeRole, String gameKey){
        node.joinTheGame(gameKey, nodeRole);
    }

    //Create new game
    private void createNewGame(SnakesProto.GameConfig config, String name, int port, SnakesProto.PlayerType type) {
        node = new Node(this, name, port, SnakesProto.PlayerType.HUMAN); //ROBOT is not available in current version of game
        node.createNewGame(config);
    }

    public void startTheGame(String name, int port, SnakesProto.PlayerType type) {
        SnakesProto.GameConfig config = Configuration.defaultConfigBuilder();
        createNewGame(config,name,port,type);
    }

    public void startTheGame(String name, int port, SnakesProto.PlayerType type, int width, int height, int foodStatic, float foodPerPlayer, int stateDelay, float deadFoodProb, int pingDelay, int nodeTimeout) {
        SnakesProto.GameConfig config = Configuration.configBuilder(width, height, foodStatic, foodPerPlayer, stateDelay, deadFoodProb, pingDelay, nodeTimeout);
        createNewGame(config,name,port,type);
    }

    public void changeRoleOnViewer(){
        node.becameAViewer();
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
    public void updateField(List<FieldPoint> field, List<String> scoresTable, String nodeRole) {
        view.updateGameView(field, scoresTable, nodeRole);
    }

    @Override
    public void updateFindGameList(List<String> games) {
        view.updateFindGameList(games);
    }

    public void leaveTheGame() {
        node.endTheGame();
    }
}
