import nsu.networks.snakes.model.Presenter;
import nsu.networks.snakes.view.View;
import nsu.networks.snakes.view.windows.MainWindow;

//System.out.println("BEFORE Album (" + System.identityHashCode(player) + "): " + player);
//System.out.println(" AFTER Album (" + System.identityHashCode(copiedAlbum) + "): " + copiedAlbum);
public class Main {
    public static void main(String[] args) {
        /*if (args.length == 1) {
            Node node = new Node(SnakesProto.GameConfig.newBuilder().build(),"Kirill",1,1500, SnakesProto.NodeRole.MASTER, SnakesProto.PlayerType.HUMAN);
        } else {
            MulticastReceiver receiver = new MulticastReceiver();
            receiver.start();
        }*/
        View view = new MainWindow();
        Presenter presenter = new Presenter(view);
        presenter.launchTheGame();
    }
}
