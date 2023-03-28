package interpreter;

import interpreter.antlr.*;
import interpreter.types.*;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

        if(value instanceof MSVal msVal){
            symbolTable.enterSymbol(id, msVal.getType(), value);
        }



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
        return new MSBool(Boolean.parseBoolean(ctx.getText()));
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
    public MSVal visitNumber(MineScriptParser.NumberContext ctx) {
        return new MSNumber(Integer.parseInt(ctx.getText()));
    }


    //TODO: Make MSNumber instead of int
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

    //TODO: Make MSNumber instead of int
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
    }
    
    @Override
    public Object visitOr(MineScriptParser.OrContext ctx) {
        var left = (boolean) visit(ctx.expression(0));
        var right = (boolean) visit(ctx.expression(1));

        return left || right;
    }

    @Override
    public Object visitFuncCall(MineScriptParser.FuncCallContext ctx) {
        var id = ctx.ID().getText();
        ArrayList<MSVal> actualParams = visitActual_parameters(ctx.actual_parameters());

        switch (id) {
            case "Step":
                // code for Step function
                break;
            case "Turn":
                // code for Turn function
                break;
            case "useBlock":
                // code for useBlock function
                break;
            case "Break":
                // code for Break function
                break;
            case "Roll":
                // code for Roll function
                break;
            case "Peek":
                // code for Peek function
                break;
            case "Sqrt":
                // code for Sqrt function
                break;
            case "Random":
                // code for Random function
                break;
            case "RandomBlock":
                // code for RandomBlock function
                break;
            case "SetSpeed":
                // code for SetSpeed function
                break;
            case "GetXPosition":
                // code for GetXPosition function
                break;
            case "GetYPosition":
                // code for GetYPosition function
                break;
            case "GetZPosition":
                // code for GetZPosition function
                break;
            case "GetHorizontalDirection":
                // code for GetHorizontalDirection function
                break;
            case "GetVerticalDirection":
                // code for GetVerticalDirection function
                break;
            case "RandomNumbers":
                // code for RandomNumbers case
                break;
            default:
                var value = symbolTable.retrieveSymbolValue(symbolTable.retrieveSymbol(id));
                if (value instanceof MSFunction function) {
                    symbolTable.enterScope();
                    var formalParams = function.getParameters();
                    for (int i = 0; i < formalParams.size(); i++) {
                        symbolTable.enterSymbol(formalParams.get(i), actualParams.get(i).getType(), actualParams.get(i));
                    }
                    visit(function.getCtx());
                    symbolTable.exitScope();
                } else {
                    throw new RuntimeException("Cannot call " + id + " because it is not a function");
                }




        }
        return null;
    }

    @Override
    public Object visitFuncDecl(MineScriptParser.FuncDeclContext ctx) {

        String id = ctx.ID().getText();
        ArrayList<String> formalParams = visitFormal_paramaters(ctx.formal_paramaters());
        formalParams.replaceAll(s -> id + "." + s);
        var statementsCtx = ctx.statements();
        MSFunction function = new MSFunction(id, formalParams, statementsCtx);
        symbolTable.enterSymbol(id, function.getType(), function);

        return null;
    }

    @Override
    public ArrayList<MSVal> visitActual_parameters(MineScriptParser.Actual_parametersContext ctx) {
        ArrayList<MSVal> actualParams = new ArrayList<MSVal>();
        for (var param : ctx.expression()) {
            actualParams.add((MSVal) visit(param));
        }
        return actualParams;
    }

    public ArrayList<String> visitFormal_paramaters(MineScriptParser.Formal_paramatersContext ctx) {
        ArrayList<String> formalParams = new ArrayList<>();
        for (var param : ctx.ID()) {
            formalParams.add(param.getText());
        }
        return formalParams;
    }
}
