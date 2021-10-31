package nsu.networks.snakes.view;

import nsu.networks.snakes.model.Presenter;

import java.util.List;

public interface View {
    void attachPresenter(Presenter presenter);

    void changeVisible(boolean var);

    void updateGameView(String field, List<String> scoresTable, String nodeRole);

    void updateFindGameList(List<String> games);

    void openField(int widthField, int heightField);
}
