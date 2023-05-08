package interpreter.types;

public class MSRelDir extends MSType {
    private final Direction direction;

    public MSRelDir(String direction) {
        super(MSTypeEnum.MSRelDir);
        switch (direction) {
            case "left" -> this.direction = Direction.LEFT;
            case "right" -> this.direction = Direction.RIGHT;
            case "up" -> this.direction = Direction.UP;
            case "down" -> this.direction = Direction.DOWN;
            default -> throw new IllegalArgumentException("Invalid direction: " + direction);
        }
    }

    public Direction getValue() {
        return direction;
    }

    @Override
    public boolean equals(MSType value) {
        return value instanceof MSRelDir relDir && relDir.getValue() == direction;
    }

    @Override
    public String toString() {
        return direction.toString().toLowerCase();
    }

    public enum Direction {
        UP,
        DOWN,
        LEFT,
        RIGHT
    }
}
