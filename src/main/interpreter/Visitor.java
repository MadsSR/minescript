package interpreter;

import interpreter.antlr.*;

public class Visitor extends MineScriptBaseVisitor<Object> {
    private final SymbolTable symbolTable = new SymbolTable();
    private final ExpressionParser parser = new ExpressionParser();

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

        return null;
    }

    @Override
    public Object visitWhile(MineScriptParser.WhileContext ctx) {
        while (parser.getBoolean(visit(ctx.expression()))) {
            visit(ctx.statements());
        }

        return null;
    }

    @Override
    public Object visitIf(MineScriptParser.IfContext ctx) {
        if (parser.getBoolean(visit(ctx.expression(0)))) {
            visit(ctx.statements(0));
        }
        else if (ctx.expression().size() > 1) {
            for(int i = 1; i < ctx.expression().size(); i++) {
                if (parser.getBoolean(visit(ctx.expression(i)))) {
                    visit(ctx.statements(i));
                }
            }
        }
        else if (ctx.statements().size() > ctx.expression().size()) {
            visit(ctx.statements(ctx.statements().size() - 1));
        }

        return null;
    }

    @Override
    public Object visitRepeat(MineScriptParser.RepeatContext ctx) {
        if (visit(ctx.expression()) instanceof Integer times) {
            for (int i = 0; i < times; i++) {
                visit(ctx.statements());
            }
        }
        else {
            throw new RuntimeException("Repeat expression must be a number");
        }

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
    public Object visitNeg(MineScriptParser.NegContext ctx) {
        return -(int) visit(ctx.expression());
    }

    @Override
    public Object visitParenExpr(MineScriptParser.ParenExprContext ctx) {
        return visit(ctx.expression());
    }

    @Override
    public Object visitNumber(MineScriptParser.NumberContext ctx) {
        return Integer.parseInt(ctx.getText());
    }
}
