package interpreter;

import interpreter.antlr.MineScriptLexer;
import interpreter.antlr.MineScriptParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class VisitorTest {
    private final Visitor visitor = new Visitor();


    @ParameterizedTest
    @ValueSource(ints = {-1000, -10, 0, 10, 1000})
    void visitAssignReturnsCorrectNumbers(int value) {
        visitor.visitAssign((MineScriptParser.AssignContext) getStmtTreeFromString("x = " + value + "\n"));
        Assertions.assertEquals(value, visitor.visitId((MineScriptParser.IdContext) getExprTreeFromString("x")));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void visitAssignReturnsCorrectBools(boolean value) {
        visitor.visitAssign((MineScriptParser.AssignContext) getStmtTreeFromString("x = " + value + "\n"));
        Assertions.assertEquals(value, visitor.visitId((MineScriptParser.IdContext) getExprTreeFromString("x")));
    }

    @Test
    void visitAssignInvalidAssignThrowsException() {
        System.setErr(null);
        Assertions.assertThrows(NullPointerException.class, () ->
                visitor.visitAssign((MineScriptParser.AssignContext) getStmtTreeFromString("x = \"hej\"\n"))
        );
    }

    @Test
    void visitWhile() {
    }

    @Test
    void visitIf() {
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 10, 100})
    void visitRepeatValidTimesEqualsNumIterations(int value) {
        visitor.visitAssign((MineScriptParser.AssignContext) getStmtTreeFromString("x = 0\n"));
        visitor.visitRepeat((MineScriptParser.RepeatContext) getStmtTreeFromString(
                """
                        repeat (%d) do
                            x = x + 1
                        endrepeat
                        """.formatted(value)
        ));
        Assertions.assertEquals(value, visitor.visitId((MineScriptParser.IdContext) getExprTreeFromString("x")));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -10, -100})
    void visitRepeatNegTimesThrowsException(int value) {
        Assertions.assertThrows(RuntimeException.class, () ->
                visitor.visitRepeat((MineScriptParser.RepeatContext) getStmtTreeFromString(
                        """
                                repeat (%d) do
                                    x = x + 1
                                endrepeat
                                """.formatted(value)
                ))
        );
    }

    @Test
    void visitRepeatNonIntTimesThrowsException() {
        Assertions.assertThrows(RuntimeException.class, () ->
                visitor.visitRepeat((MineScriptParser.RepeatContext) getStmtTreeFromString(
                        """
                                repeat ("hej") do
                                    x = x + 1
                                endrepeat
                                """
                ))
        );
    }


    @Test
    void visitBoolPassTrueExpectedTrue() {
        Assertions.assertEquals(true, visitor.visitBool((MineScriptParser.BoolContext) getExprTreeFromString("true")));
    }

    @Test
    void visitBoolPassFalseExpectedFalse() {
        Assertions.assertEquals(false, visitor.visitBool((MineScriptParser.BoolContext) getExprTreeFromString("false")));
    }

    @Test
    void visitBoolPassExceptions() {
        Assertions.assertThrows(RuntimeException.class, () -> visitor.visitBool((MineScriptParser.BoolContext) getExprTreeFromString("abc")));
    }

    @Test
    void visitCompComparesIntValuesAndReturnsBooleanExpectsTrue() {
        //Initial tests for greater than and greater than or equal
        Assertions.assertEquals(true, visitor.visitComp((MineScriptParser.CompContext) getExprTreeFromString("5 > 4")));
        Assertions.assertEquals(true, visitor.visitComp((MineScriptParser.CompContext) getExprTreeFromString("5 >= 4")));
    }

    @Test
    void visitCompComparesIntValuesAndReturnsBooleanExpectsFalse() {
        //Initial tests for less than and less than or equal
        Assertions.assertEquals(false, visitor.visitComp((MineScriptParser.CompContext) getExprTreeFromString("5 < 4")));
        Assertions.assertEquals(false, visitor.visitComp((MineScriptParser.CompContext) getExprTreeFromString("5 <= 4")));
    }

    @Test
    void visitIsIsNot() {
        Assertions.assertEquals(true, visitor.visitIsIsNot((MineScriptParser.IsIsNotContext) getExprTreeFromString("5 is 5")));
        Assertions.assertEquals(false, visitor.visitIsIsNot((MineScriptParser.IsIsNotContext) getExprTreeFromString("5 is not 5")));
    }

    @Test
    void visitId() {
    }

    @Test
    void visitAddSub() {
        Assertions.assertEquals(10, visitor.visitAddSub((MineScriptParser.AddSubContext) getExprTreeFromString("5 + 5")));
        Assertions.assertEquals(0, visitor.visitAddSub((MineScriptParser.AddSubContext) getExprTreeFromString("5 - 5")));
    }

    @Test
    void visitNumber() {
    }

    @Test
    void visitParenExpr() {
        String input = "(10-3)\n";
        Assertions.assertEquals(7, visitor.visitParenExpr((MineScriptParser.ParenExprContext) getExprTreeFromString(input)));
    }

    @Test
    void visitMultDivMod() {
        Assertions.assertEquals(25, visitor.visitMultDivMod((MineScriptParser.MultDivModContext) getExprTreeFromString("5 * 5")));
        Assertions.assertEquals(1, visitor.visitMultDivMod((MineScriptParser.MultDivModContext) getExprTreeFromString("5 / 5")));
        Assertions.assertEquals(0, visitor.visitMultDivMod((MineScriptParser.MultDivModContext) getExprTreeFromString("5 % 5")));
    }


    @Test
    void visitPow() {
        Assertions.assertEquals(25, visitor.visitPow((MineScriptParser.PowContext) getExprTreeFromString("5 ^ 2")));
        Assertions.assertEquals(1, visitor.visitPow((MineScriptParser.PowContext) getExprTreeFromString("5 ^ 0")));
        Assertions.assertEquals(0, visitor.visitPow((MineScriptParser.PowContext) getExprTreeFromString("0 ^ 5")));
        Assertions.assertEquals(1, visitor.visitPow((MineScriptParser.PowContext) getExprTreeFromString("1 ^ 5")));
    }

    @Test
    void visitPowNegativeNumberThrowsException() {
        Assertions.assertThrows(RuntimeException.class, () -> visitor.visitPow((MineScriptParser.PowContext) getExprTreeFromString("5 ^ -1")));
    }

    @Test
    void visitPowExpr() {
        Assertions.assertEquals(25, visitor.visitPow((MineScriptParser.PowContext) getExprTreeFromString("5 ^ (2 + 0 * 1)")));
    }

    @Test
    void visitPowId() {
        visitor.visitAssign((MineScriptParser.AssignContext) getStmtTreeFromString("x = 2\n"));
        Assertions.assertEquals(25, visitor.visitPow((MineScriptParser.PowContext) getExprTreeFromString("5 ^ x")));
    }

    @Test
    void visitPowStringThrowsException() {
        Assertions.assertThrows(RuntimeException.class, () -> visitor.visitPow((MineScriptParser.PowContext) getExprTreeFromString("5 ^ unknown")));
    }


    @Test
    void visitNegNegativeNumber() {
        Assertions.assertEquals(-5, visitor.visitNeg((MineScriptParser.NegContext) getExprTreeFromString("-5")));
        Assertions.assertEquals(-3335, visitor.visitNeg((MineScriptParser.NegContext) getExprTreeFromString("-3335")));
    }

    @Test
    void visitNegExpr() {
        Assertions.assertEquals(-10, visitor.visitNeg((MineScriptParser.NegContext) getExprTreeFromString("-(5 + 5)")));
        Assertions.assertEquals(-1, visitor.visitNeg((MineScriptParser.NegContext) getExprTreeFromString("-(5 / 5)")));
    }

    @Test
    void visitNegId() {
        visitor.visitAssign((MineScriptParser.AssignContext) getStmtTreeFromString("x = 5\n"));
        Assertions.assertEquals(-5, visitor.visitNeg((MineScriptParser.NegContext) getExprTreeFromString("-x")));
    }

    @Test
    void visitNegStringThrowsException() {
        Assertions.assertThrows(RuntimeException.class, () -> visitor.visitNeg((MineScriptParser.NegContext) getExprTreeFromString("-unknown")));
    }
    @Test
    void visitOrTrueOrFalseExpectsTrue() {
        Assertions.assertEquals(true, visitor.visitOr((MineScriptParser.OrContext) getExprTreeFromString("true or false")));
    }
    @Test
    void visitOrTrueOrTrueExpectsTrue() {
        Assertions.assertEquals(true, visitor.visitOr((MineScriptParser.OrContext) getExprTreeFromString("true or true")));
    }
    @Test
    void visitOrFalseOrFalseExpectsFalse() {
        Assertions.assertEquals(false, visitor.visitOr((MineScriptParser.OrContext) getExprTreeFromString("false or false")));
    }


    private MineScriptParser.ExpressionContext getExprTreeFromString(String input) {
        var lexer = new MineScriptLexer(CharStreams.fromString(input));
        var parser = new MineScriptParser(new CommonTokenStream(lexer));
        return parser.expression();
    }

    private MineScriptParser.StatementContext getStmtTreeFromString(String input) {
        var lexer = new MineScriptLexer(CharStreams.fromString(input));
        var parser = new MineScriptParser(new CommonTokenStream(lexer));
        return parser.statement();
    }
}