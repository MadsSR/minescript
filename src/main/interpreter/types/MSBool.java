package interpreter.types;

public class MSBool extends MSType {
    private final boolean value;

    public MSBool(boolean value) {
        this.value = value;
    }

    public boolean getValue() {
        return value;
    }

    @Override
    public boolean equals(MSType value) {
        if (value instanceof MSBool b) {
            return this.value == b.value;
        }
        return false;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}

