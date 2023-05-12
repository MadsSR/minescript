package interpreter;

import interpreter.antlr.MineScriptBaseVisitor;
import interpreter.antlr.MineScriptParser;
import interpreter.exceptions.SymbolNotFoundException;
import interpreter.types.*;
import minescript.block.entity.TurtleBlockEntity;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

public class Visitor extends MineScriptBaseVisitor<MSType> {
    private final ExpressionParser parser = new ExpressionParser();
    private final Random random = new Random(System.currentTimeMillis());
    private final SymbolTable symbolTable;
    private boolean hasReturned = false;
    private TurtleBlockEntity entity;
    private boolean shouldBreak = true;

    public Visitor(TurtleBlockEntity entity, SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        this.entity = entity;
    }

    public Visitor(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }

    @Override
    public MSType visitProgram(MineScriptParser.ProgramContext ctx) {
        for (MineScriptParser.StatementContext statement : ctx.statement()) {
            if (statement instanceof MineScriptParser.FuncDeclContext) {
                visit(statement);
            }
        }

        for (MineScriptParser.StatementContext statement : ctx.statement()) {
            if (!(statement instanceof MineScriptParser.FuncDeclContext)) {
                if (hasReturned) {
                    return null;
                }
                visit(statement);
            }
        }
        return null;
    }

    @Override
    public MSType visitStatements(MineScriptParser.StatementsContext ctx) {
        MSType val = null;

        if (!(ctx.getParent() instanceof MineScriptParser.FuncDeclContext)){
            symbolTable.enterScope();
        }

        for (MineScriptParser.StatementContext statement : ctx.statement()) {
            if (statement instanceof MineScriptParser.FuncDeclContext) {
                visit(statement);
            }
        }


        for (MineScriptParser.StatementContext statement : ctx.statement()) {
            if (!hasReturned){
            val = visit(statement);
            }

            if (hasReturned) {
                if (!(ctx.getParent() instanceof MineScriptParser.FuncDeclContext)){
                    symbolTable.enterScope();
                }
                return val;
            }
        }

        if (!(ctx.getParent() instanceof MineScriptParser.FuncDeclContext)){
            symbolTable.exitScope();
        }

        return null;
    }

    @Override
    public MSType visitAssign(MineScriptParser.AssignContext ctx) {
        String id = ctx.ID().getText();
        MSType value = visit(ctx.expression());
        if (value == null) {
            throw new IllegalArgumentException("Cannot assign '" + id + "' to null");
        }
        symbolTable.enterSymbol(id, value);

        return null;
    }

    @Override
    public MSType visitWhile(MineScriptParser.WhileContext ctx) {
        MSType value = null;

        while (parser.getBoolean(visit(ctx.expression())).getValue() && !hasReturned) {
            value = visit(ctx.statements());
        }

        return value;
    }

    @Override
    public MSType visitIf(MineScriptParser.IfContext ctx) {
        // Handle if and else if
        for (var expression : ctx.expression()) {
            if (parser.getBoolean(visit(expression)).getValue()) {
                return visit(ctx.statements(ctx.expression().indexOf(expression)));
            }
        }

        // Handle else
        if (ctx.expression().size() < ctx.statements().size()) {
            return visit(ctx.statements(ctx.statements().size() - 1));
        }

        return null;
    }

    @Override
    public MSType visitRepeat(MineScriptParser.RepeatContext ctx) {
        MSType value = null;

        if (visit(ctx.expression()) instanceof MSNumber times && times.getValue() >= 0) {
            for (int i = 0; i < times.getValue(); i++) {
                value = visit(ctx.statements());
                if (hasReturned) return value;
            }
        } else {
            throw new RuntimeException("Repeat expression must be a non-negative number");
        }

        return value;
    }

    @Override
    public MSType visitBool(MineScriptParser.BoolContext ctx) {
        return new MSBool(Boolean.parseBoolean(ctx.getText()));
    }


    @Override
    public MSType visitNotExpr(MineScriptParser.NotExprContext ctx) {
        return parser.getNegatedBoolean(visit(ctx.expression()));
    }

    @Override
    public MSType visitComp(MineScriptParser.CompContext ctx) {
        MSType left = visit(ctx.expression(0));
        MSType right = visit(ctx.expression(1));

        if (left instanceof MSNumber l && right instanceof MSNumber r) {
            return new MSBool(switch (ctx.op.getText()) {
                case "<" -> l.getValue() < r.getValue();
                case ">" -> l.getValue() > r.getValue();
                case "<=" -> l.getValue() <= r.getValue();
                case ">=" -> l.getValue() >= r.getValue();
                default -> throw new RuntimeException("Unknown operator: " + ctx.op.getText());
            });
        }
        throw new RuntimeException("Cannot compare " + left.getTypeName() + " and " + right.getTypeName());
    }

    @Override
    public MSType visitIsIsNot(MineScriptParser.IsIsNotContext ctx) {
        MSType left = visit(ctx.expression(0));
        MSType right = visit(ctx.expression(1));

        return new MSBool(switch (ctx.op.getText()) {
            case "is" -> left.equals(right);
            case "is not" -> !left.equals(right);
            default -> throw new RuntimeException("Unknown operator ' '" + ctx.op.getText() + "'");
        });
    }

    @Override
    public MSType visitId(MineScriptParser.IdContext ctx) {
        String id = ctx.ID().getText();
        MSType value;

        try {
            value = symbolTable.retrieveSymbolValue(symbolTable.retrieveSymbol(id));
        } catch (SymbolNotFoundException e) {
            throw new RuntimeException("Cannot reference '" + id + "' as it is not defined");
        }

        return value;
    }

    @Override
    public MSType visitBlock(MineScriptParser.BlockContext ctx) {
        String blockId = ctx.BLOCK().getText();
        return new MSBlock(blockId);
    }

    @Override
    public MSType visitRelDir(MineScriptParser.RelDirContext ctx) {
        String relDir = ctx.RELDIR().getText();
        return new MSRelDir(relDir);
    }

    @Override
    public MSType visitAbsDir(MineScriptParser.AbsDirContext ctx) {
        String absDir = ctx.ABSDIR().getText();
        return new MSAbsDir(absDir);
    }

    @Override
    public MSType visitAddSub(MineScriptParser.AddSubContext ctx) {
        MSType left = visit(ctx.expression(0));
        MSType right = visit(ctx.expression(1));

        if (left instanceof MSNumber l && right instanceof MSNumber r) {
            return new MSNumber(switch (ctx.op.getText()) {
                case "+" -> l.getValue() + r.getValue();
                case "-" -> l.getValue() - r.getValue();
                default -> throw new RuntimeException("Unknown operator: " + ctx.op.getText());
            });
        }
        throw new RuntimeException("Cannot use '" + ctx.op.getText() + "' operator on " + left.getTypeName() + " and " + right.getTypeName());
    }

    @Override
    public MSType visitNeg(MineScriptParser.NegContext ctx) {
        if (visit(ctx.expression()) instanceof MSNumber n) {
            return new MSNumber(-n.getValue());
        }
        throw new RuntimeException("Cannot negate " + visit(ctx.expression()).getTypeName());
    }

    @Override
    public MSType visitParenExpr(MineScriptParser.ParenExprContext ctx) {
        return visit(ctx.expression());
    }

    @Override
    public MSType visitNumber(MineScriptParser.NumberContext ctx) {
        return new MSNumber(Integer.parseInt(ctx.NUMBER().getText()));
    }

    @Override
    public MSType visitMultDivMod(MineScriptParser.MultDivModContext ctx) {
        MSType left = visit(ctx.expression(0));
        MSType right = visit(ctx.expression(1));

        if (left instanceof MSNumber l && right instanceof MSNumber r) {
            return new MSNumber(switch (ctx.op.getText()) {
                case "*" -> l.getValue() * r.getValue();
                case "/" -> l.getValue() / r.getValue();
                case "%" -> l.getValue() % r.getValue();
                default -> throw new RuntimeException("Unknown operator: " + ctx.op.getText());
            });
        }
        throw new RuntimeException("Cannot use '" + ctx.op.getText() + "' operator on " + left.getTypeName() + " and " + right.getTypeName());
    }

    @Override
    public MSType visitPow(MineScriptParser.PowContext ctx) {
        MSType left = visit(ctx.expression(0));
        MSType right = visit(ctx.expression(1));

        if (left instanceof MSNumber l && right instanceof MSNumber r) {
            if (r.getValue() < 0) {
                throw new RuntimeException("Cannot raise to negative power");
            }
            return new MSNumber((int) Math.pow(l.getValue(), r.getValue()));
        }

        throw new RuntimeException("Cannot use '^' operator on " + left.getTypeName() + " and " + right.getTypeName());
    }

    @Override
    public MSType visitAnd(MineScriptParser.AndContext ctx) {
        MSType left = visit(ctx.expression(0));
        if (!parser.getBoolean(left).getValue()) {
            return new MSBool(false);
        }
        MSType right = visit(ctx.expression(1));
        return new MSBool(parser.getBoolean(right).getValue());

    }

    @Override
    public MSType visitOr(MineScriptParser.OrContext ctx) {
        MSType left = visit(ctx.expression(0));
        if (parser.getBoolean(left).getValue()) {
            return new MSBool(true);
        }
        MSType right = visit(ctx.expression(1));

        return new MSBool(parser.getBoolean(right).getValue());
    }

    @Override
    public MSType visitReturn(MineScriptParser.ReturnContext ctx) {
        MSType retVal = visit(ctx.expression());
        hasReturned = true;
        if (retVal == null) {
            throw new RuntimeException("Return expression must not be null");
        }
        return retVal;
    }

    @Override
    public MSType visitFuncCall(MineScriptParser.FuncCallContext ctx) {
        var id = ctx.ID().getText();
        ArrayList<MSType> actualParams = getActualParams(ctx.actual_parameters());
        MSType retVal = null;

        if (actualParams.stream().anyMatch(Objects::isNull)) {
            throw new RuntimeException(id + "() cannot take null as an argument");
        }

        switch (id) {
            case "Step" -> {
                if (actualParams.size() != 1 || !(actualParams.get(0) instanceof MSNumber n)) {
                    throw new RuntimeException(getFuncCallErrorMessage(id, new int[]{1}, "number", actualParams));
                }
                entity.step(n.getValue());
            }
            case "Turn" -> {
                if (actualParams.size() != 1 || (!(actualParams.get(0) instanceof MSRelDir) && !(actualParams.get(0) instanceof MSAbsDir))) {
                    throw new RuntimeException(getFuncCallErrorMessage(id, new int[]{1}, "direction", actualParams));
                }
                MSType dir = actualParams.get(0);
                if (dir instanceof MSRelDir relDir) {
                    entity.turn(relDir.getValue());
                } else if (dir instanceof MSAbsDir absDir) {
                    entity.turn(absDir.getValue());
                }
            }
            case "UseBlock" -> {
                if (actualParams.size() != 1 || !(actualParams.get(0) instanceof MSBlock b)) {
                    throw new RuntimeException(getFuncCallErrorMessage(id, new int[]{1}, "block", actualParams));
                }
                entity.useBlock(b.getValue());
            }
            case "Break" -> {
                if (actualParams.size() == 0) {
                    retVal = new MSBool(shouldBreak);
                    break;
                }
                if (actualParams.size() != 1 || !(actualParams.get(0) instanceof MSBool b)) {
                    throw new RuntimeException(getFuncCallErrorMessage(id, new int[]{1}, "bool", actualParams));
                }
                entity.shouldBreak = b.getValue();
                shouldBreak = b.getValue();
                retVal = new MSBool(shouldBreak);
            }
            case "Peek" -> {
                if (actualParams.size() != 0) {
                    throw new RuntimeException(getFuncCallErrorMessage(id, new int[]{0}, "", actualParams));
                }
                retVal = new MSBlock(entity.peek());
            }
            case "Sqrt" -> {
                if (actualParams.size() != 1 || !(actualParams.get(0) instanceof MSNumber n)) {
                    throw new RuntimeException(getFuncCallErrorMessage(id, new int[]{1}, "number", actualParams));
                }
                retVal = new MSNumber((int) Math.round(Math.sqrt(n.getValue())));
            }
            case "Random" -> {
                if (actualParams.size() == 0) {
                    retVal = new MSNumber(random.nextInt());
                } else if (actualParams.size() == 1 && actualParams.get(0) instanceof MSNumber n) {
                    retVal = new MSNumber(random.nextInt(n.getValue()));
                } else {
                    throw new RuntimeException(getFuncCallErrorMessage(id, new int[]{0, 1}, "number", actualParams));
                }
            }
            case "RandomBlock" -> {
                if (actualParams.size() != 0) {
                    throw new RuntimeException(getFuncCallErrorMessage(id, new int[]{0}, "", actualParams));
                }
                retVal = new MSBlock(Registries.BLOCK.get(random.nextInt(Registries.BLOCK.size())));
            }
            case "SetSpeed" -> {
                if (actualParams.size() != 1 || !(actualParams.get(0) instanceof MSNumber n)) {
                    throw new RuntimeException(getFuncCallErrorMessage(id, new int[]{1}, "number", actualParams));
                }
                entity.setSpeed(n.getValue());
            }
            case "GetXPosition" -> {
                if (actualParams.size() != 0) {
                    throw new RuntimeException(getFuncCallErrorMessage(id, new int[]{0}, "", actualParams));
                }
                retVal = new MSNumber(entity.getXPosition());
            }
            case "GetYPosition" -> {
                if (actualParams.size() != 0) {
                    throw new RuntimeException(getFuncCallErrorMessage(id, new int[]{0}, "", actualParams));
                }
                retVal = new MSNumber(entity.getYPosition());
            }
            case "GetZPosition" -> {
                if (actualParams.size() != 0) {
                    throw new RuntimeException(getFuncCallErrorMessage(id, new int[]{0}, "", actualParams));
                }
                retVal = new MSNumber(entity.getZPosition());
            }
            case "GetHorizontalDirection" -> {
                if (actualParams.size() != 0) {
                    throw new RuntimeException(getFuncCallErrorMessage(id, new int[]{0}, "", actualParams));
                }
                retVal = new MSAbsDir(entity.getHorizontalDirection());
            }
            case "GetVerticalDirection" -> {
                if (actualParams.size() != 0) {
                    throw new RuntimeException(getFuncCallErrorMessage(id, new int[]{0}, "", actualParams));
                }
                retVal = new MSAbsDir(entity.getVerticalDirection());
            }
            case "SetCoordinates" -> {
                if (actualParams.size() != 3 || !(actualParams.get(0) instanceof MSNumber x) || !(actualParams.get(1) instanceof MSNumber y) || !(actualParams.get(2) instanceof MSNumber z)) {
                    throw new RuntimeException(getFuncCallErrorMessage(id, new int[]{3}, "number", actualParams));
                }
                entity.setPosition(new BlockPos(x.getValue(), y.getValue(), z.getValue()));
            }
            case "SetXCoordinate" -> {
                if (actualParams.size() != 1 || !(actualParams.get(0) instanceof MSNumber n)) {
                    throw new RuntimeException(getFuncCallErrorMessage(id, new int[]{1}, "number", actualParams));
                }
                entity.setPosition(new BlockPos(n.getValue(), entity.getYPosition(), entity.getZPosition()));
            }
            case "SetYCoordinate" -> {
                if (actualParams.size() != 1 || !(actualParams.get(0) instanceof MSNumber n)) {
                    throw new RuntimeException(getFuncCallErrorMessage(id, new int[]{1}, "number", actualParams));
                }
                entity.setPosition(new BlockPos(entity.getXPosition(), n.getValue(), entity.getZPosition()));
            }
            case "SetZCoordinate" -> {
                if (actualParams.size() != 1 || !(actualParams.get(0) instanceof MSNumber n)) {
                    throw new RuntimeException(getFuncCallErrorMessage(id, new int[]{1}, "number", actualParams));
                }
                entity.setPosition(new BlockPos(entity.getXPosition(), entity.getYPosition(), n.getValue()));
            }
            case "Print" -> {
                if (actualParams.size() == 0) {
                    throw new RuntimeException(id + "() takes at least 1 argument but 0 were given");
                }
                ctx.actual_parameters().expression().forEach(expressionContext -> {
                    MSType type = actualParams.get(ctx.actual_parameters().expression().indexOf(expressionContext));
                    String expressionId = expressionContext.getText();
                    String text;
                    MSMessageType messageType;

                    if (type == null) {
                        text = "null";
                        messageType = MSMessageType.WARNING;
                    } else if (type instanceof MSFunction function) {
                        text = function.getTypeName();
                        messageType = MSMessageType.INFO;
                    } else {
                        text = type.toString();
                        messageType = MSMessageType.INFO;
                    }

                    if (expressionId.equals(text)) {
                        entity.print(text, messageType);
                    } else {
                        entity.print(expressionId + " is: " + text, messageType);
                    }
                });
            }
            default -> {
                MSType value;
                try {
                    value = symbolTable.retrieveSymbolValue(symbolTable.retrieveSymbol(id));
                } catch (SymbolNotFoundException e) {
                    throw new RuntimeException("Cannot call function '" + id + "' because it is not defined");
                }
                if (!(value instanceof MSFunction function)) {
                    throw new RuntimeException("Cannot call '" + id + "' because it is not a function");
                }
                var formalParams = function.getParameters();

                if (formalParams.size() != actualParams.size()) {
                    throw new RuntimeException(getFuncCallErrorMessage(id, new int[]{formalParams.size()}, "", actualParams));
                }
                symbolTable.enterScope();

                // Bind actual params to formal params
                for (int i = 0; i < formalParams.size(); i++) {
                    formalParams.set(i, id + "." + formalParams.get(i));
                    symbolTable.enterSymbol(formalParams.get(i), actualParams.get(i));
                }
                hasReturned = false;
                retVal = visit(function.getCtx());
                hasReturned = false;
                symbolTable.exitScope();
            }
        }
        if (entity != null) entity = entity.getTurtleEntity();

        return retVal;
    }

    @Override
    public MSType visitFuncDecl(MineScriptParser.FuncDeclContext ctx) {
        ArrayList<String> formalParams = getFormalParams(ctx.formal_paramaters());
        String id = ctx.ID().getText();
        var statementsCtx = ctx.statements();
        MSFunction function = new MSFunction(id, formalParams, statementsCtx);
        symbolTable.enterSymbol(id, function);

        return null;
    }

    @Override
    public MSType visitActual_parameters(MineScriptParser.Actual_parametersContext ctx) {
        return null;
    }

    @Override
    public MSType visitFormal_paramaters(MineScriptParser.Formal_paramatersContext ctx) {
        return null;
    }

    private ArrayList<String> getFormalParams(MineScriptParser.Formal_paramatersContext ctx) {
        ArrayList<String> formalParams = new ArrayList<>();

        if (ctx == null) return formalParams;

        for (var param : ctx.ID()) {
            formalParams.add(param.getText());
        }
        return formalParams;
    }

    private ArrayList<MSType> getActualParams(MineScriptParser.Actual_parametersContext ctx) {
        ArrayList<MSType> actualParams = new ArrayList<>();

        if (ctx == null) return actualParams;

        for (var param : ctx.expression()) {
            actualParams.add(visit(param));
        }
        return actualParams;
    }

    /**
     * @param id             Function name
     * @param argumentsCount Possible number of arguments
     * @param paramTypes     Expected parameter types
     * @param actualParams   Actual parameters
     * @return Error message
     */
    private String getFuncCallErrorMessage(String id, int[] argumentsCount, String paramTypes, ArrayList<MSType> actualParams) {
        StringBuilder message = new StringBuilder(id + "() takes ");
        argumentsCount = Arrays.stream(argumentsCount).sorted().toArray();
        if (argumentsCount.length == 0) {
            message.append("no arguments");
        } else {
            for (int i = 0; i < argumentsCount.length; i++) {
                if (i > 0 && i == argumentsCount.length - 1) {
                    message.append(" or ");
                } else if (i > 0) {
                    message.append(", ");
                }
                message.append(argumentsCount[i]);
            }
            message.append(" argument");
            if (argumentsCount[argumentsCount.length - 1] != 1) {
                message.append("s");
            }
            if (!paramTypes.equals(""))
                message.append(" (").append(paramTypes).append(")");
        }
        message.append(" but ").append(actualParams.size()).append(" were given");

        String types = actualParams.stream().map(MSType::getTypeName).collect(Collectors.joining(", "));
        if (!types.isEmpty()) {
            types = " of type " + types;
        }
        message.append(types);
        return message.toString();
    }
}