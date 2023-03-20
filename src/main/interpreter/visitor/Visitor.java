package src.main.interpreter.visitor;

import src.main.interpreter.SymbolTable;
import src.main.interpreter.parser.MineScriptParser;

public class Visitor extends MineScriptBaseVisitor<Object> {
    private SymbolTable symbolTable = new SymbolTable();

    @Override
    public Object visitAssign(MineScriptParser.AssignContext ctx) {
        String id = ctx.ID().getText();
        var value = visit(ctx.expression());

        try {
            symbolTable.enterSymbol(id, value.getClass(), value);
            int hej = 0;
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    @Override
    public Object visitNumber(MineScriptParser.NumberContext ctx) {
        return Integer.parseInt(ctx.getText());
    }


}
