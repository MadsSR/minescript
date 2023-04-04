package interpreter.types;

public class MSNumber extends MSType {
    private int value;


    public MSNumber(int value) {
        super(MSTypeEnum.MSNumber);
        this.value = value;
    }

    @Override
    public MSTypeEnum getType() {
        return MSTypeEnum.MSNumber;
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
}
