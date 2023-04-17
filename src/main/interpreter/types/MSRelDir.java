package interpreter.types;

public class MSRelDir extends MSType {
    private final int degree;

    public MSRelDir(int degree) {
        super(MSTypeEnum.MSRelDir);
        this.degree = degree;
    }

    public MSRelDir(String direction) {
        super(MSTypeEnum.MSRelDir);
        switch (direction) {
            case "left" -> this.degree = -90;
            case "right" -> this.degree = 90;
            default -> this.degree = 0;
        }
    }

    public int getValue() {
        return degree;
    }

    @Override
    public boolean equals(MSType value) {
        if (value instanceof MSRelDir) {
            return ((MSRelDir) value).getValue() == degree;
        }
        return false;
    }
}
