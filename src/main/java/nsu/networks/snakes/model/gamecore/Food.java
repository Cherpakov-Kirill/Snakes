package nsu.networks.snakes.model.gamecore;

import nsu.networks.snakes.model.SnakesProto;
import nsu.networks.snakes.model.SnakesProto.GameState.Coord;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static nsu.networks.snakes.model.gamecore.GameCore.buildCoordinate;

public class Food {
    private FoodListener listener;
    private final int width;
    private final int height;
    private final int foodStatic;
    private final float foodPerPlayer;
    private int numberOfAliveSnakes;
    private int numberOfFood;
    private Set<Coord> foodCoordinates;
    private int notAddedFood;

    public void updateNumberOfFood(int numberOfAliveSnakes) {
        this.numberOfAliveSnakes = numberOfAliveSnakes;
        int newNumberOfFood = foodStatic + (int) (foodPerPlayer * numberOfAliveSnakes);
        notAddedFood += newNumberOfFood - this.numberOfFood;
        this.numberOfFood = newNumberOfFood;
    }
    public void updateFood(List<Coord> food) {
        this.foodCoordinates = new HashSet<>(food);
        for (Coord coordinate: foodCoordinates) {
            listener.setFoodPoint(coordinate);
        }
    }

    public void addAllNotAddedFood(){
        int localNotAddedFood = notAddedFood;
        for(int i=0;i<localNotAddedFood;i++){
            notAddedFood--;
            addNewFood(); //if you have not place on field then this func adds 1 food to notAddedFood
        }
        listener.updateField();
    }

    public void foodWasEaten(int x, int y) {
        Coord ateFood = buildCoordinate(x,y);
        foodCoordinates.remove(ateFood);
        addNewFood();
    }

    public List<Coord> getListOfFoodCoordinates(){
        return new LinkedList<>(foodCoordinates);
    }

    public Food(FoodListener listener, int width, int height, int foodStatic, float foodPerPlayer) {
        this.listener = listener;
        this.width = width;
        this.height = height;
        this.foodStatic = foodStatic;
        this.foodPerPlayer = foodPerPlayer;
        this.notAddedFood = 0;
        foodCoordinates = new HashSet<>();
    }

    private void addNewFood() {
        Coord newFood = randomizeFoodPosition();
        if (newFood == null) {
            notAddedFood++;
        }
        foodCoordinates.add(newFood);
        listener.setFoodPoint(newFood);
    }

    private Coord randomizeFoodPosition() {
        boolean success = false;
        String field = listener.getFieldString();
        int freePlace = field.indexOf('-');
        if (freePlace == -1) {
            return null; //have not free place on field for new food
        }
        int x = field.indexOf('-') % width;
        int y = field.indexOf('-') / height;
        int iter = 0;
        while (!success) {
            int randX = (int) (Math.random() * width);
            int randY = (int) (Math.random() * height);
            if (listener.getPointFormField(randX, randY) != '#') {
                x = randX;
                y = randY;
                success = true;
            }
            iter++;
            if (iter == 20) break;
        }
        return buildCoordinate(x, y);
    }
}
