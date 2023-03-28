package interpreter.types;

public class MSNumber extends MSVal{
    private int value;


    public MSNumber(int value) {
        super(MSType.MSNumber);
        this.value = value;
    }

    @Override
    public MSType getType() {
        return MSType.MSNumber;
    }

    public int getValue() {
        return value;
    }

    @Override
    public boolean equals(MSVal value) {
        if (value instanceof MSNumber n) {
            return this.value == n.value;
        }
        return false;
    }
}
