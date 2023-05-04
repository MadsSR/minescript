package interpreter.types;


import interpreter.antlr.MineScriptParser;

import java.util.ArrayList;
import java.util.EnumSet;

public class MSFunction extends MSType {

    private String name;
    private ArrayList<String> parameters;
    private MineScriptParser.StatementsContext ctx;

    public MSFunction(String name, ArrayList<String> parameters, MineScriptParser.StatementsContext ctx) {
        super(MSTypeEnum.MSFunction);
        if (EnumSet.allOf(MSInbuiltFunction.class).contains(MSInbuiltFunction.valueOf(name))) {
            throw new RuntimeException("Cannot redefine inbuilt function: " + name);
        }
        this.name = name;
        this.parameters = parameters;
        this.ctx = ctx;
    }

    public String getName() {
        return name;
    }

    public ArrayList<String> getParameters() {
        return parameters;
    }

    public MineScriptParser.StatementsContext getCtx() {
        return ctx;
    }

    @Override
    public MSTypeEnum getType() {
        return MSTypeEnum.MSFunction;
    }

    @Override
    public boolean equals(MSType value) {
        if (value instanceof MSFunction f) {
            return this.name.equals(f.name);
        }
        return false;
    }


}