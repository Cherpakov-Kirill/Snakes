package nsu.networks.snakes.model;

import nsu.networks.snakes.model.players.FieldPoint;

import java.util.List;

public interface NodeListener {
    void updateField(List<FieldPoint> field, List<String> scoresTable, String nodeRole);
    void updateFindGameList(List<String> games);
    void openFieldWindow(int widthField, int heightField);
}
