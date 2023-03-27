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
        visitor.visitAssign((MineScriptParser.AssignContext)getStmtTreeFromString("x = " + value + "\n"));
        Assertions.assertEquals(value, visitor.visitId((MineScriptParser.IdContext)getExprTreeFromString("x")));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void visitAssignReturnsCorrectBools(boolean value) {
        visitor.visitAssign((MineScriptParser.AssignContext)getStmtTreeFromString("x = "+ value +"\n"));
        Assertions.assertEquals(value, visitor.visitId((MineScriptParser.IdContext)getExprTreeFromString("x")));
    }

    @Test
    void visitAssignInvalidAssignThrowsException() {
        System.setErr(null);
        Assertions.assertThrows(NullPointerException.class, () ->
            visitor.visitAssign((MineScriptParser.AssignContext)getStmtTreeFromString("x = \"hej\"\n"))
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
        visitor.visitAssign((MineScriptParser.AssignContext)getStmtTreeFromString("x = 0\n"));
        visitor.visitRepeat((MineScriptParser.RepeatContext)getStmtTreeFromString(
                """
                repeat (%d) do
                    x = x + 1
                endrepeat
                """.formatted(value)
        ));
        Assertions.assertEquals(value, visitor.visitId((MineScriptParser.IdContext)getExprTreeFromString("x")));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -10, -100})
    void visitRepeatNegTimesThrowsException(int value) {
        Assertions.assertThrows(RuntimeException.class, () ->
            visitor.visitRepeat((MineScriptParser.RepeatContext)getStmtTreeFromString(
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
            visitor.visitRepeat((MineScriptParser.RepeatContext)getStmtTreeFromString(
                    """
                    repeat ("hej") do
                        x = x + 1
                    endrepeat
                    """
            ))
        );
    }


    @Test
    void visitBool() {
    }

    @Test
    void visitComp() {
    }

    @Test
    void visitId() {
    }

    @Test
    void visitAddSub() {
        Assertions.assertEquals(10, visitor.visitAddSub((MineScriptParser.AddSubContext)getExprTreeFromString("5 + 5")));
        Assertions.assertEquals(0, visitor.visitAddSub((MineScriptParser.AddSubContext)getExprTreeFromString("5 - 5")));
    }

    @Test
    void visitNumber() {
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