package interpreter.types;

public abstract class MSType {
    public String getTypeName() {
        return this.getClass().getSimpleName().replace("MS", "").toLowerCase();
    }

    public abstract boolean equals(MSType value);
}
