package nsu.networks.snakes.model.node;

import java.util.List;

public interface NodeListener {
    void updateField(String field);
    void updateFindGameList(List<String> games);
    void openFieldWindow(int widthField, int heightField);
}