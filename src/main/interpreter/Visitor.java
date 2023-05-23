package interpreter;

import interpreter.antlr.MineScriptBaseVisitor;
import interpreter.antlr.MineScriptParser;
import interpreter.exceptions.SymbolNotFoundException;
import interpreter.exceptions.ThreadInterruptedException;
import interpreter.types.*;
import minescript.network.TurtleCommands;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class Visitor extends MineScriptBaseVisitor<MSType> {
    private final Random random = new Random(System.currentTimeMillis());
    private final SymbolTable symbolTable;
    private boolean hasReturned = false;

    private final BigInteger INT_MAX = BigInteger.valueOf(Integer.MAX_VALUE);
    private final BigInteger INT_MIN = BigInteger.valueOf(Integer.MIN_VALUE);
    private Block placingBlock;
    private boolean shouldBreak;
    private int turtleDelay;
    private MinecraftServer server;
    private ServerWorld world;
    private BlockPos pos;

    /**
     * @param server      The server instance
     * @param world       The world instance
     * @param pos         The position of the turtle
     * @param symbolTable The symbol table of interpreter
     */
    public Visitor(MinecraftServer server, ServerWorld world, BlockPos pos, SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        this.placingBlock = Blocks.AIR;
        this.shouldBreak = true;
        this.turtleDelay = 200;
        this.server = server;
        this.world = world;
        this.pos = pos;
    }

    /**
     * @param symbolTable The symbol table of interpreter
     */
    public Visitor(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }

    @Override
    public MSType visitProgram(MineScriptParser.ProgramContext ctx) {
        /*Initial run of the code written in the turtle, this handles forward referencing*/
        for (MineScriptParser.StatementContext statement : ctx.statement()) {
            if (statement instanceof MineScriptParser.FuncDeclContext) {
                visit(statement);
            }
        }

        /*Runs the rest of the code after the functions have been identified*/
        for (MineScriptParser.StatementContext statement : ctx.statement()) {
            /*Runs everything except function declarations*/
            if (!(statement instanceof MineScriptParser.FuncDeclContext)) {
                /*If a return has been made in the outermost scope, it stops the program*/
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

        if (!(ctx.getParent() instanceof MineScriptParser.FuncDeclContext)) {
            symbolTable.enterScope();
        }

        for (MineScriptParser.StatementContext statement : ctx.statement()) {
            if (statement instanceof MineScriptParser.FuncDeclContext) {
                visit(statement);
            }
        }


        for (MineScriptParser.StatementContext statement : ctx.statement()) {
            if (!hasReturned) {
                val = visit(statement);
            }

            if (hasReturned) {
                if (!(ctx.getParent() instanceof MineScriptParser.FuncDeclContext)) {
                    symbolTable.exitScope();
                }
                return val;
            }
        }

        if (!(ctx.getParent() instanceof MineScriptParser.FuncDeclContext)) {
            symbolTable.exitScope();
        }

        return null;
    }

    @Override
    public MSType visitAssign(MineScriptParser.AssignContext ctx) {
        String id = ctx.ID().getText();
        MSType value = visit(ctx.expression());
        /*Null check in case of assignment to function that doesn't return*/
        if (value == null) {
            throw new IllegalArgumentException("Cannot assign '" + id + "' to null");
        }
        /*Throws error if the id is restricted*/
        symbolTable.enterSymbol(id, value);

        return null;
    }

    @Override
    public MSType visitWhile(MineScriptParser.WhileContext ctx) {
        MSType value = null;

        /*Also checks for the returned state in case a return has been called inside the loop body*/
        while (visit(ctx.expression()) instanceof MSBool c && c.getValue() && !hasReturned) {
            value = visit(ctx.statements());
        }

        if (!(visit(ctx.expression()) instanceof MSBool))
            throw new RuntimeException("While condition must be a boolean");

        return value;
    }

    @Override
    public MSType visitIf(MineScriptParser.IfContext ctx) {
        /*Handle if and else if*/
        for (var expression : ctx.expression()) {
            MSType condition = visit(expression);

            if (!(condition instanceof MSBool))
                throw new RuntimeException("If condition must be a boolean");

            if (((MSBool) condition).getValue()) {
                return visit(ctx.statements(ctx.expression().indexOf(expression)));
            }
        }

        /*Handle else*/
        if (ctx.expression().size() < ctx.statements().size()) {
            return visit(ctx.statements(ctx.statements().size() - 1));
        }

        return null;
    }

    @Override
    public MSType visitRepeat(MineScriptParser.RepeatContext ctx) {
        MSType value = null;

        /*Checks a number has been typed, and it's 0 or over*/
        if (visit(ctx.expression()) instanceof MSNumber times && times.getValue() >= 0) {
            for (int i = 0; i < times.getValue(); i++) {
                value = visit(ctx.statements());
                /*Like the while in case a return is called in the body the loop stops*/
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
        MSType value = visit(ctx.expression());

        if (!(value instanceof MSBool))
            throw new RuntimeException("Cannot negate non-boolean value");

        boolean bool = ((MSBool) value).getValue();
        return new MSBool(!bool);
    }

    @Override
    public MSType visitComp(MineScriptParser.CompContext ctx) {
        MSType left = visit(ctx.expression(0));
        MSType right = visit(ctx.expression(1));

        /*Check that both sides are numbers otherwise throw an error*/
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
            default -> throw new RuntimeException("Unknown operator: " + ctx.op.getText());
        });
    }

    @Override
    public MSType visitId(MineScriptParser.IdContext ctx) {
        String id = ctx.ID().getText();
        MSType value;

        /*Attempt to find the id from the symbol table and retrieve the value, otherwise throw an error*/
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

        /*Check that both sides are numbers otherwise throw an error*/
        if (!(left instanceof MSNumber l) || !(right instanceof MSNumber r)) {
            throw new RuntimeException("Cannot use '" + ctx.op.getText() + "' operator on " + left.getTypeName() + " and " + right.getTypeName());
        }

        return calculateArithmeticExpression(l, r, ctx.op.getText());
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
        try {
            return new MSNumber(Integer.parseInt(ctx.NUMBER().getText()));
        } catch (NumberFormatException e) {
            throw new RuntimeException("Number (" + ctx.NUMBER().getText() + ") is not within the range. Number range from " + INT_MIN + " to " + INT_MAX);
        }
    }

    @Override
    public MSType visitMultDivMod(MineScriptParser.MultDivModContext ctx) {
        MSType left = visit(ctx.expression(0));
        MSType right = visit(ctx.expression(1));

        /*Check that both sides are numbers otherwise throw an error*/
        if (!(left instanceof MSNumber l) || !(right instanceof MSNumber r)) {
            throw new RuntimeException("Cannot use '" + ctx.op.getText() + "' operator on " + left.getTypeName() + " and " + right.getTypeName());
        }

        return calculateArithmeticExpression(l, r, ctx.op.getText());
    }

    @Override
    public MSType visitPow(MineScriptParser.PowContext ctx) {
        MSType left = visit(ctx.expression(0));
        MSType right = visit(ctx.expression(1));

        if (!(left instanceof MSNumber l) || !(right instanceof MSNumber r)) {
            throw new RuntimeException("Cannot use '^' operator on " + left.getTypeName() + " and " + right.getTypeName());
        }
        if (r.getValue() < 0) {
            throw new RuntimeException("Cannot raise to negative power");
        }

        return calculateArithmeticExpression(l, r, "^");
    }

    @Override
    public MSType visitAnd(MineScriptParser.AndContext ctx) {
        /*Handles and with short-circuit, that's why the left side is handled first*/
        MSType left = visit(ctx.expression(0));

        if (left instanceof MSBool l) {/*If the left part is false, it returns without the need to evaluate the right side*/
            if (!l.getValue()) {
                return l;
            }

            MSType right = visit(ctx.expression(1));
            if (!(right instanceof MSBool r)) {
                throw new RuntimeException("Cannot use 'and' operator on " + left.getTypeName() + " and " + right.getTypeName());
            }
            return r;
        } else {
            throw new RuntimeException("Cannot use 'and' operator on " + left.getTypeName() + " and " + visit(ctx.expression(1)).getTypeName());
        }
    }

    @Override
    public MSType visitOr(MineScriptParser.OrContext ctx) {
        /*Handles or with short-circuit, much like in the visitAnd method*/
        MSType left = visit(ctx.expression(0));

        if (left instanceof MSBool l) {
            if (l.getValue()) {
                return l;
            }

            MSType right = visit(ctx.expression(1));
            if (!(right instanceof MSBool r)) {
                throw new RuntimeException("Cannot use 'or' operator on " + left.getTypeName() + " and " + right.getTypeName());
            }
            return r;
        } else {
            throw new RuntimeException("Cannot use 'or' operator on " + left.getTypeName() + " and " + visit(ctx.expression(1)).getTypeName());
        }
    }

    @Override
    public MSType visitReturn(MineScriptParser.ReturnContext ctx) {
        MSType retVal = visit(ctx.expression());
        /*Changes the bool that stops most things from proceeding*/
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

        /*Checks actual params for functions that do not return, since minescript uses pass by value, as these would not work*/
        if (actualParams.stream().anyMatch(Objects::isNull)) {
            throw new RuntimeException(id + "() cannot take null as an argument");
        }

        /*Switch if the function is a built-in function, otherwise default checks for functions in the symbol table*/
        switch (id) {
            case "Step" -> {
                /*Checks if the function got 1 parameter and that parameter is a number, otherwise throws an error*/
                if (actualParams.size() != 1 || !(actualParams.get(0) instanceof MSNumber n)) {
                    throw new RuntimeException(getFuncCallErrorMessage(id, new int[]{1}, "number", actualParams));
                }

                AtomicBoolean skip = new AtomicBoolean(false);
                CompletableFuture<BlockPos> future = CompletableFuture.completedFuture(pos);

                for (int i = 0; i < n.getValue(); i++) {
                    future = future.thenComposeAsync(prevPos -> {
                        if(skip.get()) {
                            return CompletableFuture.completedFuture(prevPos);
                        }

                        if (!skip.get() && !shouldBreak && TurtleCommands.peek(world, prevPos) != Blocks.AIR) {
                            TurtleCommands.print(server, "Cannot move forward, block in the way", MSMessageType.WARNING);
                            skip.set(true);

                            return CompletableFuture.completedFuture(prevPos);
                        }

                        return TurtleCommands.step(server, world, placingBlock, prevPos);
                    });
                    timeout(turtleDelay);

                    if (skip.get()) {
                        break;
                    }
                }

                pos = future.join();
            }
            case "Turn" -> {
                /*Checks if the function got 1 parameter and that parameter is a direction, otherwise throws an error*/
                if (actualParams.size() != 1 || (!(actualParams.get(0) instanceof MSRelDir) && !(actualParams.get(0) instanceof MSAbsDir))) {
                    throw new RuntimeException(getFuncCallErrorMessage(id, new int[]{1}, "direction", actualParams));
                }

                MSType dir = actualParams.get(0);

                if (dir instanceof MSRelDir relDir) {
                    TurtleCommands.turn(server, world, pos, relDir.getValue());
                } else if (dir instanceof MSAbsDir absDir) {
                    TurtleCommands.turn(server, world, pos, absDir.getValue());
                }

                timeout(turtleDelay);
            }
            case "UseBlock" -> {
                /*Checks if the function got 1 parameter and that parameter is a block, otherwise throws an error*/
                if (actualParams.size() != 1 || !(actualParams.get(0) instanceof MSBlock b)) {
                    throw new RuntimeException(getFuncCallErrorMessage(id, new int[]{1}, "block", actualParams));
                }

                placingBlock = b.getValue();
            }
            case "Break" -> {
                if (actualParams.size() == 0) {
                    retVal = new MSBool(shouldBreak);
                    break;
                }

                if (actualParams.size() != 1 || !(actualParams.get(0) instanceof MSBool b)) {
                    throw new RuntimeException(getFuncCallErrorMessage(id, new int[]{0, 1}, "bool", actualParams));
                }

                shouldBreak = b.getValue();
                retVal = new MSBool(shouldBreak);
            }
            case "Peek" -> {
                if (actualParams.size() != 0) {
                    throw new RuntimeException(getFuncCallErrorMessage(id, new int[]{0}, "", actualParams));
                }

                retVal = new MSBlock(TurtleCommands.peek(world, pos));
            }
            case "Sqrt" -> {
                if (actualParams.size() != 1 || !(actualParams.get(0) instanceof MSNumber n)) {
                    throw new RuntimeException(getFuncCallErrorMessage(id, new int[]{1}, "number", actualParams));
                }

                retVal = new MSNumber((int) Math.floor(Math.sqrt(n.getValue())));
            }
            case "Random" -> {
                /*If no parameter is used returns a random unbounded int*/
                if (actualParams.size() == 0) {
                    retVal = new MSNumber(random.nextInt());
                    /*If 1 parameter is used returns a random int between 0 and the parameter*/
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

                if (n.getValue() < 1 || n.getValue() > 10) {
                    throw new RuntimeException("Cannot set speed to " + n.getValue() + ", must be between 1 and 10");
                }

                this.turtleDelay = 200 - n.getValue() * 20;
            }
            case "GetXCoordinate" -> {
                if (actualParams.size() != 0) {
                    throw new RuntimeException(getFuncCallErrorMessage(id, new int[]{0}, "", actualParams));
                }

                retVal = new MSNumber(pos.getX());
            }
            case "GetYCoordinate" -> {
                if (actualParams.size() != 0) {
                    throw new RuntimeException(getFuncCallErrorMessage(id, new int[]{0}, "", actualParams));
                }

                retVal = new MSNumber(pos.getY());
            }
            case "GetZCoordinate" -> {
                if (actualParams.size() != 0) {
                    throw new RuntimeException(getFuncCallErrorMessage(id, new int[]{0}, "", actualParams));
                }

                retVal = new MSNumber(pos.getZ());
            }
            case "GetHorizontalDirection" -> {
                if (actualParams.size() != 0) {
                    throw new RuntimeException(getFuncCallErrorMessage(id, new int[]{0}, "", actualParams));
                }

                retVal = TurtleCommands.getHorizontalDirection(world, pos);
            }
            case "GetVerticalDirection" -> {
                if (actualParams.size() != 0) {
                    throw new RuntimeException(getFuncCallErrorMessage(id, new int[]{0}, "", actualParams));
                }

                retVal = TurtleCommands.getVerticalDirection(world, pos);
            }
            case "SetCoordinates" -> {
                if (actualParams.size() != 3 || !(actualParams.get(0) instanceof MSNumber x) || !(actualParams.get(1) instanceof MSNumber y) || !(actualParams.get(2) instanceof MSNumber z)) {
                    throw new RuntimeException(getFuncCallErrorMessage(id, new int[]{3}, "number", actualParams));
                }

                CompletableFuture<BlockPos> future = CompletableFuture.completedFuture(pos);
                future = future.thenComposeAsync(prevPos -> TurtleCommands.setPosition(server, world, prevPos, new BlockPos(x.getValue(), y.getValue(), z.getValue())));

                pos = future.join();
                timeout(turtleDelay);
            }
            case "SetXCoordinate" -> {
                if (actualParams.size() != 1 || !(actualParams.get(0) instanceof MSNumber n)) {
                    throw new RuntimeException(getFuncCallErrorMessage(id, new int[]{1}, "number", actualParams));
                }

                CompletableFuture<BlockPos> future = CompletableFuture.completedFuture(pos);
                future = future.thenComposeAsync(prevPos -> TurtleCommands.setPosition(server, world, prevPos, new BlockPos(n.getValue(), pos.getY(), pos.getZ())));

                pos = future.join();
                timeout(turtleDelay);
            }
            case "SetYCoordinate" -> {
                if (actualParams.size() != 1 || !(actualParams.get(0) instanceof MSNumber n)) {
                    throw new RuntimeException(getFuncCallErrorMessage(id, new int[]{1}, "number", actualParams));
                }

                CompletableFuture<BlockPos> future = CompletableFuture.completedFuture(pos);
                future = future.thenComposeAsync(prevPos -> TurtleCommands.setPosition(server, world, prevPos, new BlockPos(pos.getX(), n.getValue(), pos.getZ())));

                pos = future.join();
                timeout(turtleDelay);
            }
            case "SetZCoordinate" -> {
                if (actualParams.size() != 1 || !(actualParams.get(0) instanceof MSNumber n)) {
                    throw new RuntimeException(getFuncCallErrorMessage(id, new int[]{1}, "number", actualParams));
                }

                CompletableFuture<BlockPos> future = CompletableFuture.completedFuture(pos);
                future = future.thenComposeAsync(prevPos -> TurtleCommands.setPosition(server, world, prevPos, new BlockPos(pos.getX(), pos.getY(), n.getValue())));

                pos = future.join();
                timeout(turtleDelay);
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
                        TurtleCommands.print(server, text, messageType);
                    } else {
                        TurtleCommands.print(server, expressionId + " is: " + text, messageType);
                    }
                    timeout(1);
                });
            }
            default -> {
                MSType value;

                /*Check the symbol table for the id, otherwise throw an error*/
                try {
                    value = symbolTable.retrieveSymbolValue(symbolTable.retrieveSymbol(id));
                } catch (SymbolNotFoundException e) {
                    throw new RuntimeException("Cannot call function '" + id + "' because it is not defined");
                }

                /*Checks if the id is a function, otherwise throw an error*/
                if (!(value instanceof MSFunction function)) {
                    throw new RuntimeException("Cannot call '" + id + "' because it is not a function");
                }

                var formalParams = function.getParameters();

                if (formalParams.size() != actualParams.size()) {
                    throw new RuntimeException(getFuncCallErrorMessage(id, new int[]{formalParams.size()}, "", actualParams));
                }

                symbolTable.enterScope();

                /*Bind actual params to formal params*/
                for (int i = 0; i < formalParams.size(); i++) {
                    formalParams.set(i, id + "." + formalParams.get(i));
                    symbolTable.enterSymbol(formalParams.get(i), actualParams.get(i));
                }

                /*hasReturned is set to false before visiting the function ctx in case this has been set to true at an earlier state*/
                hasReturned = false;
                retVal = visit(function.getCtx());

                /*hasReturned is set to false after visiting the function ctx since it possibly was just used to return from the function*/
                hasReturned = false;
                symbolTable.exitScope();
            }
        }

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

    /**
     * @param ctx Formal parameters context
     * @return List of formal parameters
     */
    private ArrayList<String> getFormalParams(MineScriptParser.Formal_paramatersContext ctx) {
        ArrayList<String> formalParams = new ArrayList<>();

        if (ctx == null) return formalParams;

        for (var param : ctx.ID()) {
            formalParams.add(param.getText());
        }
        return formalParams;
    }

    /**
     * @param ctx Actual parameters context
     * @return List of actual parameters
     */
    private ArrayList<MSType> getActualParams(MineScriptParser.Actual_parametersContext ctx) {
        ArrayList<MSType> actualParams = new ArrayList<>();

        if (ctx == null) return actualParams;

        for (var param : ctx.expression()) {
            actualParams.add(visit(param));
        }
        return actualParams;
    }

    /**
     * @param left     the left side of the expression
     * @param right    the right side of the expression
     * @param operator the operator to use
     * @return the result of the expression
     * @throws RuntimeException if the result is too big or too small to be an int
     */
    private MSNumber calculateArithmeticExpression(MSNumber left, MSNumber right, String operator) {
        // Convert to BigIntegers to avoid overflow
        BigInteger leftBig = BigInteger.valueOf(left.getValue());
        BigInteger rightBig = BigInteger.valueOf(right.getValue());
        BigInteger result = switch (operator) {
            case "+" -> leftBig.add(rightBig);
            case "-" -> leftBig.subtract(rightBig);
            case "*" -> leftBig.multiply(rightBig);
            case "/" -> {
                if (rightBig.equals(BigInteger.ZERO)) {
                    throw new RuntimeException("Cannot divide by 0");
                }
                yield leftBig.divide(rightBig);
            }
            case "%" -> {
                if (rightBig.compareTo(BigInteger.ZERO) <= 0) {
                    throw new RuntimeException("Modulus must be positive");
                }
                yield leftBig.mod(rightBig);
            }
            case "^" -> leftBig.pow(rightBig.intValue());
            default -> throw new RuntimeException("Unknown operator: " + operator);
        };

        // Check if the result is in the range of an int
        if (result.compareTo(INT_MAX) > 0) {
            throw new RuntimeException("Result of '" + leftBig + " " + operator + " " + rightBig + "' is too big. Maximum number is " + Integer.MAX_VALUE);
        } else if (result.compareTo(INT_MIN) < 0) {
            throw new RuntimeException("Result of '" + leftBig + " " + operator + " " + rightBig + "' is too small. Minimum number is " + Integer.MIN_VALUE);
        }
        return new MSNumber(result.intValue());
    }

    /**
     * @param id             Function name
     * @param argumentsCount Possible number of arguments. Each index represents a possible number of arguments
     * @param paramTypes     Expected parameter types
     * @param actualParams   Actual parameters
     * @return String containing the error message
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
            if (!paramTypes.equals("")) message.append(" (").append(paramTypes).append(")");
        }
        message.append(" but ").append(actualParams.size()).append(" were given");

        String types = actualParams.stream().map(MSType::getTypeName).collect(Collectors.joining(", "));
        if (!types.isEmpty()) {
            types = " of type " + types;
        }
        message.append(types);
        return message.toString();
    }

    /**
     * @param millis Time to wait in milliseconds
     */
    private void timeout(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new ThreadInterruptedException("Thread was interrupted while waiting");
        }
    }
}