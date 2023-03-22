package src.main.interpreter.visitor;

import src.main.interpreter.SymbolTable;
import src.main.interpreter.parser.MineScriptParser;

public class Visitor extends MineScriptBaseVisitor<Object> {
    private final SymbolTable symbolTable = new SymbolTable();

    @Override
    public Object visitAssign(MineScriptParser.AssignContext ctx) {
        String id = ctx.ID().getText();
        var value = visit(ctx.expression());

        try {
            symbolTable.enterSymbol(id, value.getClass(), value);
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return value;
    }

    @Override
    public Object visitWhile(MineScriptParser.WhileContext ctx) {
        symbolTable.enterScope();
        while ((boolean) visit(ctx.expression())) {
            visit(ctx.statements());
        }
        symbolTable.exitScope();
        return null;
    }

    @Override
    public Object visitBool(MineScriptParser.BoolContext ctx) {
        return Boolean.parseBoolean(ctx.getText());
    }

    @Override
    public Object visitComp(MineScriptParser.CompContext ctx) {
        Object left = visit(ctx.expression(0));
        Object right = visit(ctx.expression(1));

        if (left instanceof Integer && right instanceof Integer) {
            return switch (ctx.op.getText()) {
                case "<" -> (int) left < (int) right;
                case ">" -> (int) left > (int) right;
                case "<=" -> (int) left <= (int) right;
                case ">=" -> (int) left >= (int) right;
                default -> throw new RuntimeException("Unknown operator: " + ctx.op.getText());
            };
        }
        throw new RuntimeException("cannot compare " + left.getClass() + " and " + right.getClass());
    }

    @Override
    public Object visitId(MineScriptParser.IdContext ctx) {
        String id = ctx.ID().getText();
        return symbolTable.retrieveSymbolValue(symbolTable.retrieveSymbol(id));
    }

    @Override
    public Object visitAddSub(MineScriptParser.AddSubContext ctx) {
        var left = (int) visit(ctx.expression(0));
        var right = (int) visit(ctx.expression(1));

        return switch (ctx.op.getText()) {
            case "+" -> left + right;
            case "-" -> left - right;
            default -> throw new RuntimeException("Unknown operator: " + ctx.op.getText());
        };
    }

    @Override
    public Object visitNumber(MineScriptParser.NumberContext ctx) {
        return Integer.parseInt(ctx.getText());
    }
}
