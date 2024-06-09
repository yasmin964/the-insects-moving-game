import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Objects;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.*;
/**
 * The main class for the insect simulation program.
 * It reads input data, initializes the game board, processes insect and food data,
 * and writes the results to an output file.
 */
public class Main {
    private static Board gameBoard;
    private static final int MIN_BOARD_SIZE = 4;
    private static final int MAX_BOARD_SIZE_1 = 1000;
    private static final int MAX_BOARD_SIZE_2 = 16;
    private static final int MAX_BOARD_SIZE_3 = 200;
    private static final int BOARD_SIZE = 3;
    /**
     * Insect data from the text document  is entered into the corresponding collection in the main class
     * @param args
     */
    public static void main(String[] args) {
        String output = "output.txt";
        try (FileWriter writer = new FileWriter(output)) {
            try {
                List<String> inputData = readFileData();

                int boardSize = Integer.parseInt(inputData.get(0));
                if (boardSize < MIN_BOARD_SIZE || boardSize > MAX_BOARD_SIZE_1) {
                    throw new InvalidBoardSizeException();
                }
                int numberOfInsects = Integer.parseInt(inputData.get(1));
                if (numberOfInsects < 1 || numberOfInsects > MAX_BOARD_SIZE_2) {
                    throw new InvalidNumberOfInsectsException();
                }
                int numberOfFoodPoints = Integer.parseInt(inputData.get(2));
                if (numberOfFoodPoints < 1 || numberOfFoodPoints > MAX_BOARD_SIZE_3) {
                    throw new InvalidNumberOfFoodPointsException();
                }
                // Initialize the game board
                gameBoard = new Board(boardSize);

                int endIndexOfLineWithInsect = BOARD_SIZE + numberOfInsects;
                if (endIndexOfLineWithInsect > inputData.size()) {
                    throw new InvalidNumberOfInsectsException();
                }
                // Process insect data starting from the fourth line
                List<String> insectData = inputData.subList(BOARD_SIZE, BOARD_SIZE + numberOfInsects);
                List<Insect> insects = createInsectCollectionAndAddToBoard(insectData, numberOfInsects);

                int startIndexOfLineWithFood = BOARD_SIZE + numberOfInsects;
                int endIndexOfLineWithFood = startIndexOfLineWithFood + numberOfFoodPoints;
                if ((endIndexOfLineWithFood) != inputData.size()) {
                    throw new InvalidNumberOfFoodPointsException();
                }

                // Process food data starting from the line after insect data
                List<String> foodData = inputData.subList(startIndexOfLineWithFood, endIndexOfLineWithFood);
                addFoodToBoard(foodData);


                for (int i = 0; i < insects.size(); i++) {
                    Insect insect = insects.get(i);
                    writer.write(InsectColor.toString(insect.color) + " ");
                    writer.write(insect.getClass().getName() + " ");
                    Direction bestDirection = insect.getBestDirection(gameBoard.getBoardData(), boardSize);
                    writer.write(bestDirection.getTextRepresentation() + " ");
                    writer.write(String.valueOf(insect.travelDirection(bestDirection,
                            gameBoard.getBoardData(), boardSize)));
                    if (i != insects.size() - 1) {
                        writer.write(System.lineSeparator());
                    }
                }

            } catch (InvalidBoardSizeException e) {
                writer.write(e.getMessage());
            } catch (InvalidNumberOfInsectsException e) {
                writer.write(e.getMessage());
            } catch (InvalidInsectColorException e) {
                writer.write(e.getMessage());
            } catch (InvalidInsectTypeException e) {
                writer.write(e.getMessage());
            } catch (DuplicateInsectException e) {
                writer.write(e.getMessage());
            } catch (TwoEntitiesOnSamePositionException e) {
                writer.write(e.getMessage());
            } catch (InvalidNumberOfFoodPointsException e) {
                writer.write(e.getMessage());
            } catch (InvalidEntityPositionException e) {
                writer.write(e.getMessage());
            } finally {
                writer.write(System.lineSeparator());
            }
        }  catch (IOException e) {
            System.out.println("Failed to read data file");
        }
    }
    /**
     * Reads data from the input file and returns it as a list of strings.
     *
     * @return A list of strings representing the data read from the input file.
     * @throws FileNotFoundException if the input file is not found.
     */
    private static List<String> readFileData() throws FileNotFoundException {
        List<String> data = new ArrayList<>();
        File file = new File("input.txt");
        Scanner scanner = new Scanner(file);

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            data.add(line);
        }

        scanner.close();
        return data;
    }
    /**
     * Processes insect data and adds insects to the game board.
     *
     * @param insectData      List of strings containing information about insects.
     * @param numberOfInsects The number of insects to process.
     * @return A list of Insect objects created from the input data.
     * @throws InvalidNumberOfInsectsException  if the number of insects is invalid.
     * @throws InvalidInsectColorException      if the insect color is invalid.
     * @throws InvalidInsectTypeException       if the insect type is invalid.
     * @throws DuplicateInsectException         if duplicate insects are found.
     * @throws InvalidEntityPositionException   if the insect's position is invalid.
     * @throws TwoEntitiesOnSamePositionException if two entities are on the same position.
     */
    private static List<Insect> createInsectCollectionAndAddToBoard(List<String> insectData, int numberOfInsects)
            throws
            InvalidNumberOfInsectsException,
            InvalidInsectColorException,
            InvalidInsectTypeException,
            DuplicateInsectException, InvalidEntityPositionException, TwoEntitiesOnSamePositionException {
        List<Insect> insects = new ArrayList<>();

        for (int i = 0; i < insectData.size(); i++) {
            String[] insectI = insectData.get(i).split(" ");
            if (insectI.length != MIN_BOARD_SIZE) {
                throw new InvalidNumberOfInsectsException();
            }
            String color = insectI[0];
            InsectColor insectColor = InsectColor.toColor(color);
            String insectType = insectI[1];
            int y = Integer.parseInt(insectI[2]);
            int x = Integer.parseInt(insectI[BOARD_SIZE]);

            if ((x < 1) || (y < 1) || (x > gameBoard.getSize()) || (y > gameBoard.getSize())) {
                throw new InvalidEntityPositionException();
            }

            EntityPosition position = new EntityPosition(x, y);

            Insect insect;
            switch (insectType) {
                case "Ant":
                    insect = new Ant(position, insectColor);
                    break;
                case "Butterfly":
                    insect = new Butterfly(position, insectColor);
                    break;
                case "Spider":
                    insect = new Spider(position, insectColor);
                    break;
                case "Grasshopper":
                    insect = new Grasshopper(position, insectColor);
                    break;
                default:
                    throw new InvalidInsectTypeException();
            }
            for (Insect ins : insects) {
                if (ins.getClass() == insect.getClass() && insectColor == ins.getColor()) {
                    throw new DuplicateInsectException();
                }
            }
            insects.add(insect);
            gameBoard.addEntity(insect);
        }

        return insects;

    }
    /**
     * Adds food entities to the game board based on the provided data.
     *
     * @param foodData A list of strings representing the data for food entities.
     *                 Each string should contain information about the amount, y-coordinate, and x-coordinate.
     * @throws InvalidInsectColorException    If the color of the insect is invalid.
     * @throws InvalidEntityPositionException If the entity position is invalid.
     * @throws DuplicateInsectException       If duplicate insects are detected.
     * @throws TwoEntitiesOnSamePositionException If two entities are in the same position.
     */
    private static void addFoodToBoard(List<String> foodData) throws InvalidInsectColorException,
            InvalidEntityPositionException, DuplicateInsectException, TwoEntitiesOnSamePositionException {
        for (String line : foodData) {
            String[] parts = line.split(" ");
            if (parts.length == BOARD_SIZE) {
                int amount = Integer.parseInt(parts[0]);
                int y = Integer.parseInt(parts[1]);
                int x = Integer.parseInt(parts[2]);

                if ((x < 1) || (y < 1) || (x > gameBoard.getSize()) || (y > gameBoard.getSize())) {
                    throw new InvalidEntityPositionException();
                }
                EntityPosition position = new EntityPosition(x, y);
                FoodPoint foodPoint = new FoodPoint(position, amount);
                gameBoard.addEntity(foodPoint);
            } else {
                System.out.println("Invalid number of food points");
            }
        }
    }
}
/**
 * Enum representing directions on the game board.
 * Each direction has a text representation, priority, and corresponding shifts in the x and y axes.
 */
enum Direction {
    N("North", 8, 0, -1),
    E("East", 7, 1, 0),
    S("South", 6, 0, 1),
    W("West", 5, -1, 0),
    NE("North-East", 4, 1, -1),
    SE("South-East", 3, 1, 1),
    SW("South-West", 2, -1, 1),
    NW("North-West", 1, -1, -1);
    private final String textRepresentation;
    private int priority;
    private int xShift;
    private int yShift;

    /**
     * Constructs a Direction with the given text representation, priority, and shifts.
     *
     * @param text      The text representation of the direction.
     * @param priority  The priority of the direction.
     * @param xShift    The shift in the x-axis.
     * @param yShift    The shift in the y-axis.
     */
    private Direction(String text, int priority, int xShift, int yShift) {
        textRepresentation = text;
        this.priority = priority;
        this.xShift = xShift;
        this.yShift = yShift;
    }

    /**
     * Gets the text representation of the direction.
     *
     * @return The text representation.
     */
    public String getTextRepresentation() {
        return textRepresentation;
    }
    /**
     * Gets the priority of the direction.
     *
     * @return The priority.
     */
    public int getPriority() {
        return priority;
    }
    /**
     * Gets the shift in the x-axis.
     *
     * @return The x-axis shift.
     */
    public int getxShift() {
        return xShift;
    }
    /**
     * Gets the shift in the y-axis.
     *
     * @return The y-axis shift.
     */
    public int getyShift() {
        return yShift;
    }
}
/**
 * Enum representing colors of insects in the game.
 * The enum provides methods to convert between String and InsectColor.
 */
enum InsectColor {
    RED,
    GREEN,
    BLUE,
    YELLOW;
    /**
     * Converts a String representation of color to InsectColor.
     *
     * @param color The String representation of color.
     * @return The corresponding InsectColor.
     * @throws InvalidInsectColorException If the provided color is invalid.
     */
    public static InsectColor toColor(String color) throws InvalidInsectColorException {
        switch (color) {
            case "Red":
                return InsectColor.RED;
            case "Green":
                return InsectColor.GREEN;
            case "Blue":
                return InsectColor.BLUE;
            case "Yellow":
                return InsectColor.YELLOW;
            default:
                throw new InvalidInsectColorException();
        }
    }
    /**
     * Converts InsectColor to its String representation.
     *
     * @param color The InsectColor to convert.
     * @return The String representation of the color.
     */
    public static String toString(InsectColor color) {
        if (color == RED) {
            return "Red";
        }
        if (color == GREEN) {
            return "Green";
        }
        if (color == BLUE) {
            return "Blue";
        } else {
            return "Yellow";
        }
    }
}
/**
 * Represents the game board that holds entities such as insects and food points.
 * The board manages the positions of entities and provides methods for interacting with them.
 *
 * @throws InvalidBoardSizeException If an invalid board size is provided during instantiation.
 */
class Board {
    private static Map<String, BoardEntity> boardData = new HashMap<>();
    private int size;
    /**
     * Constructs a new game board with the specified size.
     *
     * @param size The size of the game board.
     * @throws InvalidBoardSizeException If the provided board size is invalid.
     */
    public Board(Integer size) throws InvalidBoardSizeException {
        this.size = size;
    }
    /**
     * Gets the size of the game board.
     *
     * @return The size of the game board.
     */
    public int getSize() {
        return size;
    }
    /**
     * Adds a board entity to the game board.
     *
     * @param entity The board entity to be added.
     * @throws DuplicateInsectException        If an attempt is made to add a duplicate insect.
     * @throws TwoEntitiesOnSamePositionException If two entities are in the same position.
     * @throws InvalidEntityPositionException  If the entity position is invalid.
     */
    public void addEntity(BoardEntity entity) throws
            TwoEntitiesOnSamePositionException {
        String key = new StringBuilder()
                .append(entity.entityPosition.getX())
                .append(" ")
                .append(entity.getEntityPosition().getY()).toString();
        if (boardData.containsKey(key)) {
            throw new TwoEntitiesOnSamePositionException();
        } else {
            boardData.put(key, entity);
        }

    }
    /**
     * Gets the board entity at the specified position.
     *
     * @param position The position of the board entity.
     * @return The board entity at the specified position.
     * @throws InvalidEntityPositionException If the entity position is invalid.
     */
    public BoardEntity getEntity(EntityPosition position) throws InvalidEntityPositionException {
        String key = new StringBuilder()
                .append(position.getY())
                .append(" ")
                .append(position.getX()).toString();
        BoardEntity boardEntity = boardData.get(key);
        if (boardEntity != null) {
            return boardEntity;
        } else {
            throw new InvalidEntityPositionException();
        }
    }

    /**
     * Gets the direction of movement for the given insect.
     *
     * @param insect The insect for which to determine the direction.
     * @return The direction of movement for the insect.
     */
    public Direction getDirection(Insect insect) {
        return null;
    }

    /**
     * Gets the sum of directions for the given insect.
     *
     * @param insect The insect for which to calculate the sum of directions.
     * @return The sum of directions for the insect.
     */
    public int getDirectionSum(Insect insect) {
        return 0;
    }

    /**
     * Gets the map representing the current state of the game board.
     *
     * @return The map containing entity positions and corresponding board entities.
     */
    public static Map<String, BoardEntity> getBoardData() {
        return boardData;
    }
}

/**
 * Represents an abstract board entity with a position on the game board.
 */
abstract class BoardEntity {
    protected EntityPosition entityPosition;

    /**
     * Gets the position of the board entity.
     *
     * @return The position of the board entity.
     */
    public EntityPosition getEntityPosition() {
        return entityPosition;
    }

    /**
     * Sets the position of the board entity.
     *
     * @param entityPosition The new position of the board entity.
     */
    public void setEntityPosition(EntityPosition entityPosition) {
        this.entityPosition = entityPosition;
    }
}

/**
 * Represents a position (coordinates) on the game board.
 */
class EntityPosition {
    private int x;
    private int y;

    /**
     * Constructs a new entity position with the specified coordinates.
     *
     * @param x The x-coordinate.
     * @param y The y-coordinate.
     */
    public EntityPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Gets the x-coordinate of the entity position.
     *
     * @return The x-coordinate.
     */
    public int getX() {
        return x;
    }

    /**
     * Sets the x-coordinate of the entity position.
     *
     * @param x The new x-coordinate.
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * Gets the y-coordinate of the entity position.
     *
     * @return The y-coordinate.
     */
    public int getY() {
        return y;
    }

    /**
     * Sets the y-coordinate of the entity position.
     *
     * @param y The new y-coordinate.
     */
    public void setY(int y) {
        this.y = y;
    }

    /**
     * Checks if this entity position is equal to another object.
     *
     * @param o The object to compare with.
     * @return True if the positions are equal; false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EntityPosition that = (EntityPosition) o;
        return x == that.x && y == that.y;
    }

    /**
     * Computes the hash code of the entity position.
     *
     * @return The hash code of the entity position.
     */
    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}

/**
 * Represents a food point on the game board.
 */
class FoodPoint extends BoardEntity {
    protected int value;

    /**
     * Constructs a new food point entity with the specified position and value.
     *
     * @param position The position of the food point on the game board.
     * @param value    The value associated with the food point.
     */
    public FoodPoint(EntityPosition position, int value) {
        setEntityPosition(position);
        this.value = value;
    }
}

abstract class Insect extends BoardEntity {
    protected InsectColor color;
    protected int step = 1;
    protected static Set<Direction> orthogonalDirections = new HashSet<>();

    protected static Set<Direction> diagonalDirections = new HashSet<>();

    /**
     * This comparator lambda takes two pairs of Direction-FoodScores
     * and tells if one pair greater than second, in according to certain rules
     */

    protected static final Comparator<Map.Entry<Direction, Integer>> PATH_SCORES_COMPARATOR =
            (e1, e2) -> {
                int firstCompare = e2.getValue() - e1.getValue();
                if (firstCompare != 0) {
                    return firstCompare;
                } else {
                    return e2.getKey().getPriority() - e1.getKey().getPriority();
                }
            };

    static {
        orthogonalDirections.add(Direction.E);
        orthogonalDirections.add(Direction.W);
        orthogonalDirections.add(Direction.N);
        orthogonalDirections.add(Direction.S);

        diagonalDirections.add(Direction.SW);
        diagonalDirections.add(Direction.SE);
        diagonalDirections.add(Direction.NW);
        diagonalDirections.add(Direction.NE);
    }

    public Insect(EntityPosition position, InsectColor color) {
        this.color = color;
        this.entityPosition = position;
    }

    public InsectColor getColor() {
        return color;
    }

    public abstract Direction getBestDirection(Map<String, BoardEntity> boardData, int boardSize);

    public abstract int travelDirection(Direction dir, Map<String, BoardEntity> boardData, int boardSize);

    protected String getPositionString(EntityPosition entityPosition){
        return new StringBuilder()
                .append(entityPosition.getX())
                .append(" ")
                .append(entityPosition.getY()).toString();
    }

    public int getOrthogonalDirectionVisible(
            Direction dir,
            EntityPosition entityPosition,
            Map<String, BoardEntity> boardData,
            int boardSize
    ) {
        int newX = entityPosition.getX() + (step * dir.getxShift());
        int newY = entityPosition.getY() + (step * dir.getyShift());

        int foodPoints = 0;
        while (newX > 0 && newX <= boardSize && newY > 0 && newY <= boardSize) {
            EntityPosition ep = new EntityPosition(newX, newY);
            BoardEntity nextStepEntity = boardData.get(getPositionString(ep));
            if (nextStepEntity instanceof FoodPoint) {
                foodPoints += ((FoodPoint) nextStepEntity).value;
            }
            newX = newX + (step * dir.getxShift());
            newY = newY + (step * dir.getyShift());
        }
        return foodPoints;
    }

    public int travelOrthogonally(
            Direction dir,
            EntityPosition entityPosition,
            InsectColor color,
            Map<String, BoardEntity> boardData,
            int boardSize
    ) {
        int newX = entityPosition.getX();
        int newY = entityPosition.getY();

        int foodPoints = 0;
        while (newX > 0 && newX <= boardSize && newY > 0 && newY <= boardSize) {
            newX = newX + (step * dir.getxShift());
            newY = newY + (step * dir.getyShift());
            EntityPosition ep = new EntityPosition(newX, newY);
            BoardEntity nextStepEntity = boardData.get(getPositionString(ep));
            if (nextStepEntity instanceof FoodPoint) {
                foodPoints += ((FoodPoint) nextStepEntity).value;
                boardData.remove(getPositionString(ep));
            }
            if (nextStepEntity instanceof Insect) {
                Insect meetInsect = (Insect) nextStepEntity;
                if (meetInsect.color != this.color) {
                    break;
                }
            }
        }

        boardData.remove(getPositionString(entityPosition)); // remove insect from board after death or reaching of the border

        return foodPoints;
    }

    public int getDiagonalDirectionVisible(
            Direction dir,
            EntityPosition entityPosition,
            Map<String, BoardEntity> boardData,
            int boardSize
    ) {
        return getOrthogonalDirectionVisible(dir, entityPosition, boardData, boardSize);
    }

    public int travelDiagonally(
            Direction dir,
            EntityPosition position,
            InsectColor color,
            Map<String, BoardEntity> boardData,
            int boardSize
    ) {
        return travelOrthogonally(dir, position, color, boardData, boardSize);
    }

}

class Butterfly extends Insect implements OrthogonalMoving {
    public Butterfly(EntityPosition position, InsectColor color) throws InvalidInsectTypeException {
        super(position, color);
    }

    @Override
    public Direction getBestDirection(Map<String, BoardEntity> boardData, int boardSize) {
        Map<Direction, Integer> pathsScores = new HashMap<>();


        for (Direction direction : orthogonalDirections) {
            pathsScores.put(direction, getOrthogonalDirectionVisible(
                    direction,
                    entityPosition,
                    boardData,
                    boardSize
            ));
        }


        List<Map.Entry<Direction, Integer>> pathsScoresList = new ArrayList<>(pathsScores.entrySet());
        Collections.sort(pathsScoresList, PATH_SCORES_COMPARATOR);

        return pathsScoresList.get(0).getKey();
    }

    @Override
    public int travelDirection(Direction dir, Map<String, BoardEntity> boardData, int boardSize) {
        int scores = 0;

        scores = travelOrthogonally(dir, entityPosition, color, boardData, boardSize);

        return scores;
    }
}

class Ant extends Insect implements DiagonalMoving, OrthogonalMoving {

    public Ant(EntityPosition position, InsectColor color) throws InvalidInsectTypeException {
        super(position, color);
    }

    @Override
    public Direction getBestDirection(Map<String, BoardEntity> boardData, int boardSize) {
        Map<Direction, Integer> pathsScores = new HashMap<>();

        for (Direction direction : orthogonalDirections) {
            pathsScores.put(direction, getOrthogonalDirectionVisible(
                    direction,
                    entityPosition,
                    boardData,
                    boardSize
            ));
        }

        for (Direction direction : diagonalDirections) {
            pathsScores.put(direction, getDiagonalDirectionVisible(
                    direction,
                    entityPosition,
                    boardData,
                    boardSize
            ));
        }


        List<Map.Entry<Direction, Integer>> pathsScoresList = new ArrayList<>(pathsScores.entrySet());
        Collections.sort(pathsScoresList, PATH_SCORES_COMPARATOR);

        return pathsScoresList.get(0).getKey();
    }

    @Override
    public int travelDirection(Direction dir, Map<String, BoardEntity> boardData, int boardSize) {
        int scores = 0;
        if (orthogonalDirections.contains(dir)) {
            scores = travelOrthogonally(dir, entityPosition, color, boardData, boardSize);
        }
        if (diagonalDirections.contains(dir)) {
            scores = travelDiagonally(dir, entityPosition, color, boardData, boardSize);
        }
        return scores;
    }
}

class Spider extends Insect implements DiagonalMoving {
    public Spider(EntityPosition position, InsectColor color) throws InvalidInsectTypeException {
        super(position, color);
    }

    @Override
    public Direction getBestDirection(Map<String, BoardEntity> boardData, int boardSize) {
        Map<Direction, Integer> pathsScores = new HashMap<>();
        for (Direction direction : diagonalDirections) {
            pathsScores.put(direction, getDiagonalDirectionVisible(
                    direction,
                    entityPosition,
                    boardData,
                    boardSize
            ));
        }


        List<Map.Entry<Direction, Integer>> pathsScoresList = new ArrayList<>(pathsScores.entrySet());
        Collections.sort(pathsScoresList, PATH_SCORES_COMPARATOR);

        return pathsScoresList.get(0).getKey();
    }

    @Override
    public int travelDirection(Direction dir, Map<String, BoardEntity> boardData, int boardSize) {
        int scores = 0;

        scores = travelDiagonally(dir, entityPosition, color, boardData, boardSize);

        return scores;
    }

}

class Grasshopper extends Insect {
    public Grasshopper(EntityPosition position, InsectColor color) throws InvalidInsectTypeException {
        super(position, color);
        this.step = 2;
    }

    @Override
    public Direction getBestDirection(Map<String, BoardEntity> boardData, int boardSize) {
        Map<Direction, Integer> pathsScores = new HashMap<>();


        for (Direction direction : orthogonalDirections) {
            pathsScores.put(direction, getOrthogonalDirectionVisible(
                    direction,
                    entityPosition,
                    boardData,
                    boardSize
            ));
        }


        List<Map.Entry<Direction, Integer>> pathsScoresList = new ArrayList<>(pathsScores.entrySet());
        Collections.sort(pathsScoresList, PATH_SCORES_COMPARATOR);

        return pathsScoresList.get(0).getKey();
    }

    @Override
    public int travelDirection(Direction dir, Map<String, BoardEntity> boardData, int boardSize) {
        int scores = 0;

        scores = travelOrthogonally(dir, entityPosition, color, boardData, boardSize);

        return scores;
    }

}

/**
 * Interface for insects that move orthogonally on the game board.
 */
interface OrthogonalMoving {
    /**
     * Gets the visibility in the orthogonal direction for the specified insect.
     *
     * @param dir             The direction to check visibility.
     * @param entityPosition  The current position of the insect.
     * @param boardData       The map containing the entities on the board.
     * @param boardSize       The size of the game board.
     * @return The visibility in the specified orthogonal direction.
     */
    int getOrthogonalDirectionVisible(Direction dir, EntityPosition entityPosition,
                                      Map<EntityPosition, BoardEntity> boardData, int boardSize);

    /**
     * Moves the insect orthogonally in the specified direction on the game board.
     *
     * @param dir             The direction to move orthogonally.
     * @param entityPosition  The current position of the insect.
     * @param color           The color of the insect.
     * @param boardData       The map containing the entities on the board.
     * @param boardSize       The size of the game board.
     * @return The score obtained by moving in the specified orthogonal direction.
     */
    int travelOrthogonally(Direction dir, EntityPosition entityPosition, InsectColor color,
                           Map<EntityPosition, BoardEntity> boardData, int boardSize);
}

/**
 * Interface for insects that move diagonally on the game board.
 */
interface DiagonalMoving {
    /**
     * Gets the visibility in the diagonal direction for the specified insect.
     *
     * @param dir             The direction to check visibility.
     * @param entityPosition  The current position of the insect.
     * @param boardData       The map containing the entities on the board.
     * @param boardSize       The size of the game board.
     * @return The visibility in the specified diagonal direction.
     */
    int getDiagonalDirectionVisible(Direction dir, EntityPosition entityPosition,
                                    Map<EntityPosition, BoardEntity> boardData, int boardSize);

    /**
     * Moves the insect diagonally in the specified direction on the game board.
     *
     * @param dir             The direction to move diagonally.
     * @param position        The current position of the insect.
     * @param color           The color of the insect.
     * @param boardData       The map containing the entities on the board.
     * @param boardSize       The size of the game board.
     * @return The score obtained by moving in the specified diagonal direction.
     */
    int travelDiagonally(Direction dir, EntityPosition position, InsectColor color,
                         Map<EntityPosition, BoardEntity> boardData, int boardSize);
}

/**
 * Exception indicating an invalid board size.
 */
class InvalidBoardSizeException extends Exception {
    @Override
    public String getMessage() {
        return "Invalid board size";
    }
}

/**
 * Exception indicating an invalid number of insects.
 */
class InvalidNumberOfInsectsException extends Exception {
    @Override
    public String getMessage() {
        return "Invalid number of insects";
    }
}

/**
 * Exception indicating an invalid number of food points.
 */
class InvalidNumberOfFoodPointsException extends Exception {
    @Override
    public String getMessage() {
        return "Invalid number of food points";
    }
}

/**
 * Exception indicating an invalid insect color.
 */
class InvalidInsectColorException extends Exception {
    @Override
    public String getMessage() {
        return "Invalid insect color";
    }
}

/**
 * Exception indicating an invalid insect type.
 */
class InvalidInsectTypeException extends Exception {
    @Override
    public String getMessage() {
        return "Invalid insect type";
    }
}

/**
 * Exception indicating an invalid entity position.
 */
class InvalidEntityPositionException extends Exception {
    @Override
    public String getMessage() {
        return "Invalid entity position";
    }
}

/**
 * Exception indicating duplicate insects on the game board.
 */
class DuplicateInsectException extends Exception {
    @Override
    public String getMessage() {
        return "Duplicate insects";
    }
}

/**
 * Exception indicating two entities occupying the same position on the game board.
 */
class TwoEntitiesOnSamePositionException extends Exception {
    @Override
    public String getMessage() {
        return "Two entities in the same position";
    }
}
