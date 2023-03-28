package interpreter.types;

public class MSBool extends MSVal{
    private boolean value;

    public MSBool(boolean value) {
        super(MSType.MSBool);
        this.value = value;
    }

    public boolean getValue() {
        return value;
    }

    @Override
    public MSType getType() {
        return MSType.MSBool;
    }

    @Override
    public boolean equals(MSVal value) {
        if (value instanceof MSBool b) {
            return this.value == b.value;
        }
        return false;
    }

}

