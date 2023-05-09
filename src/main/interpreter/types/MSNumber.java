package interpreter.types;

public class MSNumber extends MSType {
    private final int value;


    public MSNumber(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public boolean equals(MSType value) {
        if (value instanceof MSNumber n) {
            return this.value == n.value;
        }
        return false;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
