package interpreter.types;

public abstract class MSType {
    private final MSTypeEnum type;

    public MSType(MSTypeEnum type) {
        this.type = type;
    }

    public MSTypeEnum getType() {
        return type;
    }

    public abstract boolean equals(MSType value);

}
