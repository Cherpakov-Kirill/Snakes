package nsu.networks.snakes.view;

import nsu.networks.snakes.model.Presenter;

import java.util.List;

public interface View {
    void attachPresenter(Presenter presenter);

    void changeVisible(boolean var);

    void updateField(String field);

    void updateFindGameList(List<String> games);

    void openField(int widthField, int heightField);
}
