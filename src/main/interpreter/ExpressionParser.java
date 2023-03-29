package interpreter;

import interpreter.antlr.MineScriptBaseVisitor;
import interpreter.types.MSBool;
import interpreter.types.MSNumber;

public class ExpressionParser extends MineScriptBaseVisitor<Object> {

    public <T> MSBool getBoolean(T ctx) {
        if (ctx instanceof MSBool b) {
            return b;
        } else if (ctx instanceof MSNumber i) {
            return new MSBool(i.getValue() != 0);
        } else {
            throw new RuntimeException("Condition must be a bool");
        }
    }

    public <T> MSBool getNegatedBoolean(T ctx) {
        if (ctx instanceof MSBool b) {
            return new MSBool(!b.getValue());
        } else if (ctx instanceof MSNumber i) {
            return new MSBool(i.getValue() == 0);
        } else {
            throw new RuntimeException("Condition must be a bool");
        }
    }

}
