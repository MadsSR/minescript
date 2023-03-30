package interpreter;

import interpreter.antlr.*;
import interpreter.exceptions.SymbolNotFoundException;
import interpreter.types.*;

import java.util.ArrayList;

public class Visitor extends MineScriptBaseVisitor<MSVal> {
    private final SymbolTable symbolTable = new SymbolTable();
    private final ExpressionParser parser = new ExpressionParser();
    private boolean hasReturned = false;
    private boolean inFunctionDecl = false;

    @Override
    public MSVal visitProgram(MineScriptParser.ProgramContext ctx) {
        for (MineScriptParser.StatementContext statement : ctx.statement()) {
            if (statement instanceof MineScriptParser.FuncDeclContext) {
                visit(statement);
            }
        }

        for (MineScriptParser.StatementContext statement : ctx.statement()) {
            if (!(statement instanceof MineScriptParser.FuncDeclContext)) {
                visit(statement);
            }
        }
        return null;
    }

    @Override
    public MSVal visitStatements(MineScriptParser.StatementsContext ctx) {
        MSVal val;
        for (MineScriptParser.StatementContext statement : ctx.statement()) {
            val = visit(statement);
            if (hasReturned) {
                return val;
            }
        }
        return null;
    }
    @Override
    public MSVal visitAssign(MineScriptParser.AssignContext ctx) {
        String id = ctx.ID().getText();
        MSVal value = visit(ctx.expression());

        symbolTable.enterSymbol(id, value.getType(), value);

        return null;
    }

    @Override
    public MSVal visitWhile(MineScriptParser.WhileContext ctx) {
        symbolTable.enterScope();
        while (parser.getBoolean(visit(ctx.expression())).getValue()) {
            visit(ctx.statements());
        }
        symbolTable.exitScope();

        return null;
    }

    @Override
    public MSVal visitIf(MineScriptParser.IfContext ctx) {
        // Handle if and else if
        for (var expression : ctx.expression()) {
            if (parser.getBoolean(visit(expression)).getValue()) {
                visit(ctx.statements(ctx.expression().indexOf(expression)));
                return null;
            }
        }

        // Handle else
        if (ctx.expression().size() < ctx.statements().size()) {
            visit(ctx.statements(ctx.statements().size() - 1));
        }

        return null;
    }

    @Override
    public MSVal visitRepeat(MineScriptParser.RepeatContext ctx) {
        if (visit(ctx.expression()) instanceof MSNumber times && times.getValue() >= 0) {
            for (int i = 0; i < times.getValue(); i++) {
                visit(ctx.statements());
            }
        } else {
            throw new RuntimeException("Repeat expression must be a non-negative number");
        }

        return null;
    }

    @Override
    //Boolean visitor for boolean values
    public MSVal visitBool(MineScriptParser.BoolContext ctx) {
        return new MSBool(Boolean.parseBoolean(ctx.getText()));
    }


    @Override
    public MSVal visitNotExpr(MineScriptParser.NotExprContext ctx) {
        return parser.getNegatedBoolean(visit(ctx.expression()));
    }

    @Override
    public MSVal visitComp(MineScriptParser.CompContext ctx) {
        MSVal left = visit(ctx.expression(0));
        MSVal right = visit(ctx.expression(1));

        if (left instanceof MSNumber l && right instanceof MSNumber r) {
            return new MSBool(switch (ctx.op.getText()) {
                case "<" -> l.getValue() < r.getValue();
                case ">" -> l.getValue() > r.getValue();
                case "<=" -> l.getValue() <= r.getValue();
                case ">=" -> l.getValue() >= r.getValue();
                default -> throw new RuntimeException("Unknown operator: " + ctx.op.getText());
            });
        }
        throw new RuntimeException("cannot compare " + left.getClass() + " and " + right.getClass());
    }

    @Override
    public MSVal visitIsIsNot(MineScriptParser.IsIsNotContext ctx) {
        MSVal left = visit(ctx.expression(0));
        MSVal right = visit(ctx.expression(1));

        return new MSBool(switch (ctx.op.getText()) {
            case "is" -> left.equals(right);
            case "is not" -> !left.equals(right);
            default -> throw new RuntimeException("Unknown operator: " + ctx.op.getText());
        });
    }

    @Override
    public MSVal visitId(MineScriptParser.IdContext ctx) {
        String id = ctx.ID().getText();
        return symbolTable.retrieveSymbolValue(symbolTable.retrieveSymbol(id));
    }

    @Override
    public MSVal visitAddSub(MineScriptParser.AddSubContext ctx) {
        MSVal left = visit(ctx.expression(0));
        MSVal right = visit(ctx.expression(1));

        if (left instanceof MSNumber l && right instanceof MSNumber r) {
            return new MSNumber(switch (ctx.op.getText()) {
                case "+" -> l.getValue() + r.getValue();
                case "-" -> l.getValue() - r.getValue();
                default -> throw new RuntimeException("Unknown operator: " + ctx.op.getText());
            });
        }
        throw new RuntimeException("cannot add / subtract " + left.getClass() + " and " + right.getClass());

    }

    @Override
    public MSVal visitNeg(MineScriptParser.NegContext ctx) {
        if (visit(ctx.expression()) instanceof MSNumber n) {
            return new MSNumber(-n.getValue());
        }
        throw new RuntimeException("Cannot negate " + visit(ctx.expression()).getClass());
    }

    @Override
    public MSVal visitParenExpr(MineScriptParser.ParenExprContext ctx) {
        return visit(ctx.expression());
    }

    @Override
    public MSVal visitNumber(MineScriptParser.NumberContext ctx) {
        return new MSNumber(Integer.parseInt(ctx.getText()));
    }

    @Override
    public MSVal visitMultDivMod(MineScriptParser.MultDivModContext ctx) {
        MSVal left = visit(ctx.expression(0));
        MSVal right = visit(ctx.expression(1));

        if (left instanceof MSNumber l && right instanceof MSNumber r) {
            return new MSNumber(switch (ctx.op.getText()) {
                case "*" -> l.getValue() * r.getValue();
                case "/" -> l.getValue() / r.getValue();
                case "%" -> l.getValue() % r.getValue();
                default -> throw new RuntimeException("Unknown operator: " + ctx.op.getText());
            });
        }
        throw new RuntimeException("Cannot multiply / divide / mod " + left.getClass() + " and " + right.getClass());
    }

    @Override
    public MSVal visitPow(MineScriptParser.PowContext ctx) {
        MSVal left = visit(ctx.expression(0));
        MSVal right = visit(ctx.expression(1));

        if (left instanceof MSNumber l && right instanceof MSNumber r) {
            if (r.getValue() < 0) {
                throw new RuntimeException("Cannot raise to negative power");
            }
            return new MSNumber((int) Math.pow(l.getValue(), r.getValue()));
        }

        throw new RuntimeException("Cannot raise " + left.getClass() + " to the power of " + right.getClass());
    }

    @Override
    public MSVal visitAnd(MineScriptParser.AndContext ctx) {
        MSVal left = visit(ctx.expression(0));
        MSVal right = visit(ctx.expression(1));

        return new MSBool(parser.getBoolean(left).getValue() && parser.getBoolean(right).getValue());
    }

    @Override
    public MSVal visitOr(MineScriptParser.OrContext ctx) {
        MSVal left = visit(ctx.expression(0));
        MSVal right = visit(ctx.expression(1));

        return new MSBool(parser.getBoolean(left).getValue() || parser.getBoolean(right).getValue());
    }

    @Override
    public MSVal visitReturn(MineScriptParser.ReturnContext ctx) {
        if (!inFunctionDecl) {
            throw new RuntimeException("Cannot return outside of define block");
        }
        hasReturned = true;
        return visit(ctx.expression());
    }

    @Override
    public MSVal visitFuncCall(MineScriptParser.FuncCallContext ctx) {
        var id = ctx.ID().getText();
        ArrayList<MSVal> actualParams = getActualParams(ctx.actual_parameters());
        MSVal retVal = null;
        inFunctionDecl = true;

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
                MSVal value;

                try {
                    value = symbolTable.retrieveSymbolValue(symbolTable.retrieveSymbol(id));
                }
                catch (SymbolNotFoundException e) {
                    throw new RuntimeException("Cannot call " + id + " because it is not declared");
                }

                if (value instanceof MSFunction function) {
                    var formalParams = function.getParameters();

                    if (formalParams.size() != actualParams.size()) {
                        throw new RuntimeException("Cannot call " + id + " because it has " + formalParams.size() + " parameters, but " + actualParams.size() + " were given");
                    }

                    symbolTable.enterScope();

                    // Bind actual params to formal params
                    for (int i = 0; i < formalParams.size(); i++) {
                        formalParams.set(i, id + "." + formalParams.get(i));
                        symbolTable.enterSymbol(formalParams.get(i), actualParams.get(i).getType(), actualParams.get(i));
                    }

                    retVal = visit(function.getCtx());
                    hasReturned = false;
                    symbolTable.exitScope();
                }
                else {
                    throw new RuntimeException("Cannot call " + id + " because it is not a function");
                }
        }
        inFunctionDecl = false;
        return retVal;
    }

    @Override
    public MSVal visitFuncDecl(MineScriptParser.FuncDeclContext ctx) {
        ArrayList<String> formalParams = getFormalParams(ctx.formal_paramaters());
        String id = ctx.ID().getText();
        var statementsCtx = ctx.statements();
        MSFunction function = new MSFunction(id, formalParams, statementsCtx);
        symbolTable.enterSymbol(id, function.getType(), function);

        return null;
    }

    @Override
    public MSVal visitActual_parameters(MineScriptParser.Actual_parametersContext ctx) {
        return null;
    }

    @Override
    public MSVal visitFormal_paramaters(MineScriptParser.Formal_paramatersContext ctx) {
        return null;
    }

    private ArrayList<String> getFormalParams(MineScriptParser.Formal_paramatersContext ctx) {
        ArrayList<String> formalParams = new ArrayList<>();

        if (ctx == null)
            return formalParams;

        for (var param : ctx.ID()) {
            formalParams.add(param.getText());
        }
        return formalParams;
    }

    private ArrayList<MSVal> getActualParams(MineScriptParser.Actual_parametersContext ctx) {
        ArrayList<MSVal> actualParams = new ArrayList<>();

        if (ctx == null)
            return actualParams;

        for (var param : ctx.expression()) {
            actualParams.add(visit(param));
        }
        return actualParams;
    }

}
