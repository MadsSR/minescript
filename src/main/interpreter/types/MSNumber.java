package interpreter.types;

public class MSNumber extends MSVal{
    private int value;

    public MSNumber(int value) {
        super(MSType.MSNumber);
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public MSType getType() {
        return MSType.MSNumber;
    }

}
