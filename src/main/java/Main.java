import nsu.networks.snakes.model.Presenter;
import nsu.networks.snakes.view.View;
import nsu.networks.snakes.view.windows.MainWindow;

public class Main {
    public static void main(String[] args) {
        View view = new MainWindow();
        Presenter presenter = new Presenter(view);
        presenter.launchUI();
    }
}
