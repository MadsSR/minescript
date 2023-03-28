package interpreter.types;

public abstract class MSVal {
    private MSType type;
    public MSVal(MSType type) {
        this.type = type;
    }

    public MSType getType(){
        return type;
    }

}
