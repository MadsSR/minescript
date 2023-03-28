package interpreter;

import interpreter.antlr.MineScriptBaseVisitor;
import interpreter.types.MSBool;

public class ExpressionParser extends MineScriptBaseVisitor<Object> {

    public <T> MSBool getBoolean(T ctx) {
        if (ctx instanceof Boolean b) {
            return new MSBool(b);
        } else if (ctx instanceof Integer i) {
            return new MSBool(i != 0);
        } else {
            throw new RuntimeException("Condition must be a bool");
        }
    }

    public <T> MSBool getNegatedBoolean(T ctx) {
        if (ctx instanceof Boolean b) {
            return new MSBool(!b);
        } else if (ctx instanceof Integer i) {
            return new MSBool(i == 0);
        } else {
            throw new RuntimeException("Condition must be a bool");
        }
    }

}
