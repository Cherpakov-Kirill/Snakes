package nsu.networks.snakes.model;

import java.util.List;

public interface NodeListener {
    void updateField(String field, List<String> scoresTable, String nodeRole);
    void updateFindGameList(List<String> games);
    void openFieldWindow(int widthField, int heightField);
}
