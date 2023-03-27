package interpreter;

import interpreter.antlr.*;

public class Visitor extends MineScriptBaseVisitor<Object> {
    private final SymbolTable symbolTable = new SymbolTable();
    private final ExpressionParser parser = new ExpressionParser();

    @Override
    public Object visitStatements(MineScriptParser.StatementsContext ctx) {
        for (MineScriptParser.StatementContext statement : ctx.statement()) {
            visit(statement);
        }

        return null;
    }
    @Override
    public Object visitAssign(MineScriptParser.AssignContext ctx) {
        String id = ctx.ID().getText();
        var value = visit(ctx.expression());

        symbolTable.enterSymbol(id, value.getClass(), value);

        return null;
    }

    @Override
    public Object visitWhile(MineScriptParser.WhileContext ctx) {
        symbolTable.enterScope();
        while (parser.getBoolean(visit(ctx.expression()))) {
            visit(ctx.statements());
        }
        symbolTable.exitScope();

        return null;
    }

    @Override
    public Object visitIf(MineScriptParser.IfContext ctx) {
        if (parser.getBoolean(visit(ctx.expression(0)))) {
            visit(ctx.statements(0));
        } else if (ctx.expression().size() > 1) {
            for (int i = 1; i < ctx.expression().size(); i++) {
                if (parser.getBoolean(visit(ctx.expression(i)))) {
                    visit(ctx.statements(i));
                }
            }
        } else if (ctx.statements().size() > ctx.expression().size()) {
            visit(ctx.statements(ctx.statements().size() - 1));
        }

        return null;
    }

    @Override
    public Object visitRepeat(MineScriptParser.RepeatContext ctx) {
        if (visit(ctx.expression()) instanceof Integer times && times >= 0) {
            for (int i = 0; i < times; i++) {
                visit(ctx.statements());
            }
        }
        else {
            throw new RuntimeException("Repeat expression must be a non-negative number");
        }

        return null;
    }

    @Override
    //Boolean visitor for boolean values
    public Object visitBool(MineScriptParser.BoolContext ctx) {
        if (ctx.getText().equals("true") || ctx.getText().equals("false")) {
            return Boolean.parseBoolean(ctx.getText());
        } else {
            throw new RuntimeException("Boolean value must be true or false");
        }
        //return Boolean.parseBoolean(ctx.getText());*/
    }


    @Override
    public Object visitNotExpr(MineScriptParser.NotExprContext ctx) {
        return !parser.getBoolean(visit(ctx.expression()));
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
    public Object visitIsIsNot(MineScriptParser.IsIsNotContext ctx) {
        Object left = visit(ctx.expression(0));
        Object right = visit(ctx.expression(1));

        return switch (ctx.op.getText()) {
            case "is" -> left == right;
            case "is not" -> left != right;
            default -> throw new RuntimeException("Unknown operator: " + ctx.op.getText());
        };
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


    @Override
    public Object visitMultDivMod(MineScriptParser.MultDivModContext ctx) {
        var left = (int) visit(ctx.expression(0));
        var right = (int) visit(ctx.expression(1));

        return switch (ctx.op.getText()) {
            case "*" -> left * right;
            case "/" -> left / right;
            case "%" -> left % right;
            default -> throw new RuntimeException("Unknown operator: " + ctx.op.getText());
        };
    }

    @Override
    public Object visitPow(MineScriptParser.PowContext ctx) {
        var left = (int) visit(ctx.expression(0));
        var right = (int) visit(ctx.expression(1));

        if (right < 0) {
            throw new RuntimeException("Cannot raise to negative power");
        }

        return (int) Math.pow(left, right);
    }

    @Override
    public Object visitAnd(MineScriptParser.AndContext ctx) {
        var left = (boolean) visit(ctx.expression(0));
        var right = (boolean) visit(ctx.expression(1));

        return left && right;
    
    @Override
    public Object visitOr(MineScriptParser.OrContext ctx) {
        var left = (boolean) visit(ctx.expression(0));
        var right = (boolean) visit(ctx.expression(1));

        return left || right;
    }
}
