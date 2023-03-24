package interpreter;

import interpreter.antlr.MineScriptBaseVisitor;

public class ExpressionParser extends MineScriptBaseVisitor<Object> {

    public <T> boolean getBoolean(T ctx) {
        if (ctx instanceof Boolean b) {
            return b;
        }
        else if (ctx instanceof Integer i) {
            return i != 0;
        }
        else {
            throw new RuntimeException("While condition must be a bool");
        }
    }

}
