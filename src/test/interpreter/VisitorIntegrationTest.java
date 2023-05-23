package interpreter;

import interpreter.antlr.MineScriptLexer;
import interpreter.antlr.MineScriptParser;
import interpreter.types.MSBool;
import interpreter.types.MSNumber;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static interpreter.utils.TreeFromString.*;

class VisitorIntegrationTest {
    private final SymbolTable symbolTable = new SymbolTable();
    private final Visitor visitor = new Visitor(symbolTable);

    @ParameterizedTest
    @ValueSource(ints = {-1000, -10, 0, 10, 1000})
    void visitAssignReturnsCorrectNumbers(int value) {
        visitor.visitAssign((MineScriptParser.AssignContext) getStmtTreeFromString("x = " + value + "\n"));
        Assertions.assertEquals(value, ((MSNumber) visitor.visitId((MineScriptParser.IdContext) getExprTreeFromString("x"))).getValue());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void visitAssignReturnsCorrectBoolean(boolean value) {
        visitor.visitAssign((MineScriptParser.AssignContext) getStmtTreeFromString("x = " + value + "\n"));
        Assertions.assertEquals(value, ((MSBool) visitor.visitId((MineScriptParser.IdContext) getExprTreeFromString("x"))).getValue());
    }

    @Test
    void visitAssignInvalidAssignThrowsException() {
        System.setErr(null);
        Assertions.assertThrows(NullPointerException.class, () ->
                visitor.visitAssign((MineScriptParser.AssignContext) getStmtTreeFromString("x = \"hej\"\n"))
        );
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 5, 10, 15})
    void visitWhileTrueExprCorrectNumIterations(int value) {
        String input = """
                x = 0
                while (x < %d) do
                    x = x + 1
                endwhile
                """.formatted(value);

        visitor.visit(getProgTreeFromString(input));
        Assertions.assertEquals(value, ((MSNumber) symbolTable.retrieveSymbolValue(symbolTable.retrieveSymbol("x"))).getValue());
    }

    @Test
    void visitWhileFalseExprSkipDo() {
        String input = """
                x = 0
                while (false) do
                    x = x + 1
                endwhile
                """;

        visitor.visit(getProgTreeFromString(input));
        Assertions.assertEquals(0, ((MSNumber) symbolTable.retrieveSymbolValue(symbolTable.retrieveSymbol("x"))).getValue());
    }

    @Test
    void visitWhileInvalidExprThrowsException() {
        String input = """
                while (abcd) do
                    x = x + 1
                endwhile
                """;

        Assertions.assertThrows(RuntimeException.class, () -> visitor.visitWhile((MineScriptParser.WhileContext) getStmtTreeFromString(input)));
    }

    @Test
    void visitIfIfEvaluatesTrue() {
        String input = """
                x = 0
                if (x is 0) do
                    x = 1
                endif
                """;

        visitor.visit(getProgTreeFromString(input));
        Assertions.assertEquals(1, ((MSNumber) symbolTable.retrieveSymbolValue(symbolTable.retrieveSymbol("x"))).getValue());
    }

    @Test
    void visitIfIfEvaluatesFalse() {
        String input = """
                x = 1
                if (x is 0) do
                    x = 1
                endif
                """;

        visitor.visit(getProgTreeFromString(input));
        Assertions.assertEquals(1, ((MSNumber) symbolTable.retrieveSymbolValue(symbolTable.retrieveSymbol("x"))).getValue());
    }

    @Test
    void visitIfElseIfEvaluatesTrue() {
        String input = """
                x = 0
                if (x is 0) do
                    x = 1
                else do
                    x = 2
                endif
                """;

        visitor.visit(getProgTreeFromString(input));
        Assertions.assertEquals(1, ((MSNumber) symbolTable.retrieveSymbolValue(symbolTable.retrieveSymbol("x"))).getValue());
    }

    @Test
    void visitIfElseIfEvaluatesFalse() {
        String input = """
                x = 1
                if (x is 0) do
                    x = 1
                else do
                    x = 2
                endif
                """;

        visitor.visit(getProgTreeFromString(input));
        Assertions.assertEquals(2, ((MSNumber) symbolTable.retrieveSymbolValue(symbolTable.retrieveSymbol("x"))).getValue());
    }

    @Test
    void visitIfElseIfElseIfEvaluatesTrue() {
        String input = """
                x = 0
                if (x is 0) do
                    x = 1
                else if (x is 1) do
                    x = 2
                else do
                    x = 3
                endif
                """;

        visitor.visit(getProgTreeFromString(input));
        Assertions.assertEquals(1, ((MSNumber) symbolTable.retrieveSymbolValue(symbolTable.retrieveSymbol("x"))).getValue());
    }

    @Test
    void visitIfElseIfElseElseIfEvaluatesTrue() {
        String input = """
                x = 2
                if (x is 0) do
                    x = 1
                else if (x is 1) do
                    x = 2
                else if (x is 2) do
                    x = 3
                else do
                    x = 4
                endif
                """;

        visitor.visit(getProgTreeFromString(input));
        Assertions.assertEquals(3, ((MSNumber) symbolTable.retrieveSymbolValue(symbolTable.retrieveSymbol("x"))).getValue());
    }

    @Test
    void visitIfElseIfElseElseIfEvaluatesFalse() {
        String input = """
                x = 3
                if (x is 0) do
                    x = 1
                else if (x is 1) do
                    x = 2
                else if (x is 2) do
                    x = 3
                else do
                    x = 4
                endif
                """;

        visitor.visit(getProgTreeFromString(input));
        Assertions.assertEquals(4, ((MSNumber) symbolTable.retrieveSymbolValue(symbolTable.retrieveSymbol("x"))).getValue());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 10, 100})
    void visitRepeatValidTimesEqualsNumIterations(int value) {
        String input = """
                x = 0
                repeat (%d) do
                    x = x + 1
                endrepeat
                """.formatted(value);

        visitor.visit(getProgTreeFromString(input));
        Assertions.assertEquals(value, ((MSNumber) symbolTable.retrieveSymbolValue(symbolTable.retrieveSymbol("x"))).getValue());
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -10, -100})
    void visitRepeatNegTimesThrowsException(int value) {
        String input =  """
                repeat (%d) do
                    x = x + 1
                endrepeat
                """.formatted(value);

        Assertions.assertThrows(RuntimeException.class, () ->
                visitor.visitRepeat((MineScriptParser.RepeatContext) getStmtTreeFromString(input))
        );
    }

    @Test
    void visitRepeatNonIntTimesThrowsException() {
        String input = """
                repeat ("hej") do
                    x = x + 1
                endrepeat
                """;

        Assertions.assertThrows(RuntimeException.class, () ->
                visitor.visitRepeat((MineScriptParser.RepeatContext) getStmtTreeFromString(input))
        );
    }

    @Test
    void visitCompComparesIntValuesAndReturnsBooleanExpectsTrue() {
        Assertions.assertTrue(((MSBool) visitor.visitComp((MineScriptParser.CompContext) getExprTreeFromString("5 > 4"))).getValue());
        Assertions.assertTrue(((MSBool) visitor.visitComp((MineScriptParser.CompContext) getExprTreeFromString("5 >= 4"))).getValue());
    }

    @Test
    void visitCompComparesIntValuesAndReturnsBooleanExpectsFalse() {
        Assertions.assertFalse(((MSBool) visitor.visitComp((MineScriptParser.CompContext) getExprTreeFromString("5 < 4"))).getValue());
        Assertions.assertFalse(((MSBool) visitor.visitComp((MineScriptParser.CompContext) getExprTreeFromString("5 <= 4"))).getValue());
    }

    @Test
    void visitIsIsNotTestsIf5is5ExpectsTrue() {
        Assertions.assertTrue(((MSBool) visitor.visitIsIsNot((MineScriptParser.IsIsNotContext) getExprTreeFromString("5 is 5"))).getValue());
    }

    @Test
    void visitIsIsNotTestsIf5isNot5ExpectsFalse() {
        Assertions.assertFalse(((MSBool) visitor.visitIsIsNot((MineScriptParser.IsIsNotContext) getExprTreeFromString("5 is not 5"))).getValue());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 4, 8, 12})
    void visitAddSub(int value) {
        Assertions.assertEquals(2*value, ((MSNumber) visitor.visitAddSub((MineScriptParser.AddSubContext) getExprTreeFromString(String.format("%d + %d", value, value)))).getValue());
        Assertions.assertEquals(0, ((MSNumber) visitor.visitAddSub((MineScriptParser.AddSubContext) getExprTreeFromString(String.format("%d - %d", value, value)))).getValue());
    }

    @Test
    void visitParenExpr() {
        String input = "(10-3)\n";
        Assertions.assertEquals(7, ((MSNumber) visitor.visitParenExpr((MineScriptParser.ParenExprContext) getExprTreeFromString(input))).getValue());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 4, 8, 12})
    void visitMultDivMod(int value) {
        Assertions.assertEquals(value * value, ((MSNumber) visitor.visitMultDivMod((MineScriptParser.MultDivModContext) getExprTreeFromString(String.format("%d * %d", value, value)))).getValue());
        Assertions.assertEquals(1, ((MSNumber) visitor.visitMultDivMod((MineScriptParser.MultDivModContext) getExprTreeFromString(String.format("%d / %d", value, value)))).getValue());
        Assertions.assertEquals(0, ((MSNumber) visitor.visitMultDivMod((MineScriptParser.MultDivModContext) getExprTreeFromString(String.format("%d %% %d", value,value)))).getValue());
    }

    @Test
    void visitPow() {
        Assertions.assertEquals(25, ((MSNumber) visitor.visitPow((MineScriptParser.PowContext) getExprTreeFromString("5 ^ 2"))).getValue());
        Assertions.assertEquals(1, ((MSNumber) visitor.visitPow((MineScriptParser.PowContext) getExprTreeFromString("5 ^ 0"))).getValue());
        Assertions.assertEquals(0, ((MSNumber) visitor.visitPow((MineScriptParser.PowContext) getExprTreeFromString("0 ^ 5"))).getValue());
        Assertions.assertEquals(1, ((MSNumber) visitor.visitPow((MineScriptParser.PowContext) getExprTreeFromString("1 ^ 5"))).getValue());
    }

    @Test
    void visitPowNegativeNumberThrowsException() {
        Assertions.assertThrows(RuntimeException.class, () -> visitor.visitPow((MineScriptParser.PowContext) getExprTreeFromString("5 ^ -1")));
    }

    @Test
    void visitPowExpr() {
        Assertions.assertEquals(25, ((MSNumber) visitor.visitPow((MineScriptParser.PowContext) getExprTreeFromString("5 ^ (2 + 0 * 1)"))).getValue());
    }

    @Test
    void visitPowId() {
        visitor.visitAssign((MineScriptParser.AssignContext) getStmtTreeFromString("x = 2\n"));
        Assertions.assertEquals(25, ((MSNumber) visitor.visitPow((MineScriptParser.PowContext) getExprTreeFromString("5 ^ x"))).getValue());
    }

    @Test
    void visitPowStringThrowsException() {
        Assertions.assertThrows(RuntimeException.class, () -> visitor.visitPow((MineScriptParser.PowContext) getExprTreeFromString("5 ^ unknown")));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 4, 8, 12})
    void visitNegExpr(int value) {
        Assertions.assertEquals(-(2*value), ((MSNumber) visitor.visitNeg((MineScriptParser.NegContext) getExprTreeFromString(String.format("-(%d + %d)", value, value)))).getValue());
        Assertions.assertEquals(-1, ((MSNumber) visitor.visitNeg((MineScriptParser.NegContext) getExprTreeFromString(String.format("-(%d / %d)", value, value)))).getValue());
    }

    @Test
    void visitNegId() {
        visitor.visitAssign((MineScriptParser.AssignContext) getStmtTreeFromString("x = 5\n"));
        Assertions.assertEquals(-5, ((MSNumber) visitor.visitNeg((MineScriptParser.NegContext) getExprTreeFromString("-x"))).getValue());
    }

    @Test
    void visitOrFalseOutcomeReturnsFalse() {
        Assertions.assertFalse(((MSBool) visitor.visitOr((MineScriptParser.OrContext) getExprTreeFromString("false or false"))).getValue());
    }

    @ParameterizedTest
    @ValueSource(strings = {"true or false", "true or true", "false or true"})
    void visitOrTrueOutcomeReturnsTrue(String input) {
        Assertions.assertTrue(((MSBool) visitor.visitOr((MineScriptParser.OrContext) getExprTreeFromString(input))).getValue());
    }

    @Test
    void visitAndTrueOutcomeReturnsTrue() {
        Assertions.assertTrue(((MSBool) visitor.visitAnd((MineScriptParser.AndContext) getExprTreeFromString("true and true"))).getValue());
    }

    @ParameterizedTest
    @ValueSource(strings = {"true and false", "false and true", "false and false"})
    void visitAndFalseOutcomeReturnsFalse(String input) {
        Assertions.assertFalse(((MSBool) visitor.visitAnd((MineScriptParser.AndContext) getExprTreeFromString(input))).getValue());
    }

    @Test
    void visitFuncCallNoFormalParamsNoActualParamReturnsValue() {
        visitor.visitFuncDecl((MineScriptParser.FuncDeclContext) getStmtTreeFromString(
                """
                        define test() do
                            return 5
                        enddefine
                        """
        ));
        Assertions.assertEquals(5, ((MSNumber) visitor.visitFuncCall((MineScriptParser.FuncCallContext) getExprTreeFromString("test()"))).getValue());
    }

    @Test
    void visitFuncCallNoFormalParamsOneActualParamThrowsException() {
        visitor.visitFuncDecl((MineScriptParser.FuncDeclContext) getStmtTreeFromString(
                """
                        define test() do
                            return 5
                        enddefine
                        """
        ));
        Assertions.assertThrows(RuntimeException.class, () -> visitor.visitFuncCall((MineScriptParser.FuncCallContext) getExprTreeFromString("test(5)")));
    }

    @Test
    void visitFuncCallOneFormalParamsNoActualParamsThrowsException() {
        visitor.visitFuncDecl((MineScriptParser.FuncDeclContext) getStmtTreeFromString(
                """
                        define test(x) do
                            return 5
                        enddefine
                        """
        ));
        Assertions.assertThrows(RuntimeException.class, () -> visitor.visitFuncCall((MineScriptParser.FuncCallContext) getExprTreeFromString("test()")));
    }

    @Test
    void visitFuncCallOneFormalParamOneActualParamReturnsVariable() {
        visitor.visitFuncDecl((MineScriptParser.FuncDeclContext) getStmtTreeFromString(
                """
                        define test(x) do
                            return x
                        enddefine
                        """
        ));
        Assertions.assertEquals(5, ((MSNumber) visitor.visitFuncCall((MineScriptParser.FuncCallContext) getExprTreeFromString("test(5)"))).getValue());
    }

    @Test
    void visitFuncCallOneFormalParamMultipleActualParamsThrowsException() {
        visitor.visitFuncDecl((MineScriptParser.FuncDeclContext) getStmtTreeFromString(
                """
                        define test(x) do
                            return x
                        enddefine
                        """
        ));
        Assertions.assertThrows(RuntimeException.class, () -> visitor.visitFuncCall((MineScriptParser.FuncCallContext) getExprTreeFromString("test(5, 6)")));
    }

    @Test
    void visitFuncCallMultipleFormalParamsOneActualParamThrowsException() {
        visitor.visitFuncDecl((MineScriptParser.FuncDeclContext) getStmtTreeFromString(
                """
                        define test(x, y) do
                            return x
                        enddefine
                        """
        ));
        Assertions.assertThrows(RuntimeException.class, () -> visitor.visitFuncCall((MineScriptParser.FuncCallContext) getExprTreeFromString("test(5)")));
    }

    @Test
    void visitFuncCallMultipleFormalParamsMultipleActualParamsReturnsValue() {
        visitor.visitFuncDecl((MineScriptParser.FuncDeclContext) getStmtTreeFromString(
                """
                        define test(x, y) do
                            z = x + y
                            return z
                        enddefine
                        """
        ));
        Assertions.assertEquals(11, ((MSNumber) visitor.visitFuncCall((MineScriptParser.FuncCallContext) getExprTreeFromString("test(5, 6)"))).getValue());
    }


    /*
     * SCOPING TESTS
     * these following tests are for checking that the scoping rules are working as expected.
     * */

    @Test
    void funcLocalParamBeforeGlobal() {
        int value1 = 123, value2 = 456;
        String input = """
                a = %d
                define test(a) do
                    a = a + 1
                    return a
                enddefine
                
                b = test(%d)
                """.formatted(value1, value2);

        visitor.visit(getProgTreeFromString(input));
        Assertions.assertEquals(value2 + 1, ((MSNumber) symbolTable.retrieveSymbolValue(symbolTable.retrieveSymbol("b"))).getValue());
    }

    @Test
    void funcLocalScopeOuterAccessThrowsRuntimeException() {
        String input = """
                define test() do
                    x = 123
                enddefine
                
                test()
                a = x
                """;

        Assertions.assertThrows(RuntimeException.class, () -> visitor.visit(getProgTreeFromString(input)));
    }

    @Test
    void funcLocalScopeInnerAccessReturnsValue() {
        int value = 123;
        String input = """
                a = 0
                define test() do
                    x = %d
                    a = x + 1
                enddefine
                
                test()
                """.formatted(value);

        visitor.visit(getProgTreeFromString(input));
        Assertions.assertEquals(value + 1, ((MSNumber) symbolTable.retrieveSymbolValue(symbolTable.retrieveSymbol("a"))).getValue());
    }


    @Test
    void visitRepeatLocalScopeInnerAccessReturnsValue() {
        int value = 123;
        String input = """
                a = 0
                repeat (1) do
                    x = %d
                    a = x + 1
                endrepeat
                """.formatted(value);

        Assertions.assertDoesNotThrow(() -> visitor.visit(getProgTreeFromString(input)));
        Assertions.assertEquals(value + 1, ((MSNumber) symbolTable.retrieveSymbolValue(symbolTable.retrieveSymbol("a"))).getValue());
    }

    @Test
    void visitRepeatLocalScopeOuterAccessThrowsRuntimeException() {
        String input = """
                repeat (1) do
                    x = 123
                endrepeat
                a = x
                """;

        Assertions.assertThrows(RuntimeException.class, () -> visitor.visit(getProgTreeFromString(input)));
    }

    @Test
    void visitWhileLocalScopeInnerAccessReturnsValue() {
        int value = 123;
        String input = """
                i = 0
                a = 0
                while (i < 1) do
                    x = %d
                    a = x + 1
                    i = i + 1
                endwhile
                """.formatted(value);

        Assertions.assertDoesNotThrow(() -> visitor.visit(getProgTreeFromString(input)));
        Assertions.assertEquals(1, ((MSNumber) symbolTable.retrieveSymbolValue(symbolTable.retrieveSymbol("i"))).getValue());
        Assertions.assertEquals(value + 1, ((MSNumber) symbolTable.retrieveSymbolValue(symbolTable.retrieveSymbol("a"))).getValue());
    }

    @Test
    void visitWhileLocalScopeOuterAccessThrowsRuntimeException() {
        String input = """
                i = 0
                while (i < 1) do
                    x = 123
                    i = i + 1
                endwhile
                a = x
                """;

        Assertions.assertThrows(RuntimeException.class, () -> visitor.visit(getProgTreeFromString(input)));
    }

    @Test
    void visitIfLocalScopeInnerAccessReturnsValue() {
        int value = 123;
        String input = """
                a = 0
                if (true) do
                    x = %d
                    a = x + 1
                endif
                """.formatted(value);

        Assertions.assertDoesNotThrow(() -> visitor.visit(getProgTreeFromString(input)));
        Assertions.assertEquals(value + 1, ((MSNumber) symbolTable.retrieveSymbolValue(symbolTable.retrieveSymbol("a"))).getValue());
    }

    @Test
    void visitIfLocalScopeOuterAccessThrowsRuntimeException() {
        String input = """
                if (true) do
                    x = 123
                endif
                a = x
                """;

        Assertions.assertThrows(RuntimeException.class, () -> visitor.visit(getProgTreeFromString(input)));
    }

    @Test
    void integrationTestParserDoesNotThrowErrors(){
        String program = """
                a = 0
                x = 0
                if (true) do
                    x = 123
                endif
                a = x
                """;

        CharStream input = CharStreams.fromString(program + System.lineSeparator());
        MineScriptLexer lexer = new MineScriptLexer(input);
        lexer.removeErrorListeners();
        lexer.addErrorListener(InterpreterErrorListener.INSTANCE);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MineScriptParser parser = new MineScriptParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(InterpreterErrorListener.INSTANCE);
        ParseTree tree = parser.program();

        String expectedOutput = "(program (statement a = (expression 0) \\n) (statement x = (expression 0) \\n) (statement if ( (expression true) ) (statements do \\n (statement x = (expression 123) \\n)) endif \\n) (statement a = (expression x) \\n\\n) <EOF>)";

        Assertions.assertEquals(expectedOutput, tree.toStringTree(parser));
        Assertions.assertDoesNotThrow(() -> visitor.visit(tree));
    }

    @Test
    void integrationTestVisitorThrowsTreeError(){
        String program = """
                a = 0
                if (true) do
                    x = 123
                endif
                a = x
                """;

        CharStream input = CharStreams.fromString(program + System.lineSeparator());
        MineScriptLexer lexer = new MineScriptLexer(input);
        lexer.removeErrorListeners();
        lexer.addErrorListener(InterpreterErrorListener.INSTANCE);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MineScriptParser parser = new MineScriptParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(InterpreterErrorListener.INSTANCE);
        ParseTree tree = parser.program();

        String expectedOutput = "(program (statement a = (expression 0) \\n) (statement if ( (expression true) ) (statements do \\n (statement x = (expression 123) \\n)) endif \\n) (statement a = (expression x) \\n\\n) <EOF>)";

        Assertions.assertEquals(expectedOutput, tree.toStringTree(parser));
        Assertions.assertThrows(RuntimeException.class, () -> visitor.visit(tree));
    }

    @Test
    void integrationTestParserThrowsTreeError(){
        String program = """
                a = 0
                x = 0
                if (true)
                    x = 123
                endif
                a = x
                Print(1)
                """;

        CharStream input = CharStreams.fromString(program + System.lineSeparator());
        MineScriptLexer lexer = new MineScriptLexer(input);
        lexer.removeErrorListeners();
        lexer.addErrorListener(InterpreterErrorListener.INSTANCE);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MineScriptParser parser = new MineScriptParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(InterpreterErrorListener.INSTANCE);
        Assertions.assertThrows(ParseCancellationException.class, () -> parser.program());

    }

}