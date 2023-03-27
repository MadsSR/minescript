package interpreter;

import interpreter.antlr.MineScriptLexer;
import interpreter.antlr.MineScriptParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.*;

class VisitorTest {
    private final Visitor visitor = new Visitor();

    @Test
    void visitAssign() {
        visitor.visitAssign((MineScriptParser.AssignContext) getStmtTreeFromString("x = 5\n"));
        Assertions.assertEquals(5, visitor.visitId((MineScriptParser.IdContext) getExprTreeFromString("x")));
    }

    @Test
    void visitWhile() {
    }

    @Test
    void visitIf() {
    }

    @Test
    void visitRepeat() {
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
        Assertions.assertEquals(10, visitor.visitAddSub((MineScriptParser.AddSubContext) getExprTreeFromString("5 + 5")));
        Assertions.assertEquals(0, visitor.visitAddSub((MineScriptParser.AddSubContext) getExprTreeFromString("5 - 5")));
    }

    @Test
    void visitNumber() {
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

//    @Test
//    void visitPowExpr() {
//        Assertions.assertEquals(25, visitor.visitPow((MineScriptParser.PowContext) getExprTreeFromString("5 ^ (2 + 0 * 1)")));
//    }

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
        Assertions.assertEquals(-2, visitor.visitNeg((MineScriptParser.NegContext) getExprTreeFromString("-(5 / 5)")));
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
