import nsu.networks.snakes.model.SnakesProto;
import nsu.networks.snakes.model.Configuration;
import nsu.networks.snakes.model.Node;
import nsu.networks.snakes.model.NodeListener;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ChangePlayerRole implements NodeListener {
    private int newPLayer1;
    private int newPLayer2;
    private Node node;

    private void init() {
        this.node = new Node(this,"Kirill",1025, SnakesProto.PlayerType.HUMAN);
        node.createNewGame(Configuration.defaultConfigBuilder());
        this.newPLayer1 = node.receiveJoinMsg("Artem","127.0.0.1",1027, SnakesProto.NodeRole.NORMAL, SnakesProto.PlayerType.HUMAN);
        this.newPLayer2 = node.receiveJoinMsg("Dima","127.0.0.1",1028, SnakesProto.NodeRole.NORMAL, SnakesProto.PlayerType.HUMAN);
    }

    @Test
    public void test(){
        init();
        //node.changePlayerRole(newPLayer1, SnakesProto.NodeRole.VIEWER);
        System.out.println("Success!");
    }

    @Override
    public void updateField(String field, List<String> scoresTable, String nodeRole) {

    }

    @Override
    public void updateFindGameList(List<String> games) {

    }

    @Override
    public void openFieldWindow(int widthField, int heightField) {

    }
}
