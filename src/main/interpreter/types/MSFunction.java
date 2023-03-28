package interpreter.types;


import interpreter.antlr.MineScriptParser;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

public class MSFunction extends MSVal{

    private String name;
    private ArrayList<String> parameters;
    private MineScriptParser.StatementsContext ctx;

    public MSFunction(String name, ArrayList<String> parameters, MineScriptParser.StatementsContext ctx) {
        super(MSType.MSFunction);
        if (EnumSet.allOf(MSInbuiltFunction.class).contains(name)){
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
    public MSType getType() {
        return MSType.MSFunction;
    }

    @Override
    public boolean equals(MSVal value) {
        if (value instanceof MSFunction f) {
            return this.name.equals(f.name);
        }
        return false;
    }


}