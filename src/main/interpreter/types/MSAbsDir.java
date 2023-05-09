package interpreter.types;

public class MSAbsDir extends MSType {
    private final Direction direction;

    public MSAbsDir(String direction) {
        switch (direction) {
            case "north" -> this.direction = Direction.NORTH;
            case "south" -> this.direction = Direction.SOUTH;
            case "west" -> this.direction = Direction.WEST;
            case "east" -> this.direction = Direction.EAST;
            case "top" -> this.direction = Direction.TOP;
            case "bottom" -> this.direction = Direction.BOTTOM;
            default -> throw new IllegalArgumentException("Invalid direction: " + direction);
        }
    }

    public Direction getValue() {
        return direction;
    }

    @Override
    public boolean equals(MSType value) {
        return value instanceof MSAbsDir absDir && absDir.getValue() == direction;
    }

    @Override
    public String toString() {
        return direction.toString().toLowerCase();
    }

    public enum Direction {
        NORTH, SOUTH, WEST, EAST, TOP, BOTTOM
    }
}
