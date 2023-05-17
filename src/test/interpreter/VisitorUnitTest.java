package interpreter;

import interpreter.antlr.MineScriptParser;
import interpreter.types.*;
import interpreter.utils.MockTerminalNode;
import interpreter.utils.MockToken;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.atomic.AtomicReference;

@ExtendWith(MockitoExtension.class)
class VisitorUnitTest {
    private final SymbolTable symbolTable = new SymbolTable();
    private final Visitor visitor = new Visitor(symbolTable);

    @Spy private final Visitor spyVisitor = visitor;
    @Mock private MineScriptParser.AssignContext mockAssignContext;
    @Mock private MineScriptParser.ExpressionContext mockExpressionContext;
    @Mock private MineScriptParser.ExpressionContext mockExpressionContext1;
    @Mock private MineScriptParser.ExpressionContext mockExpressionContext2;
    @Mock private MineScriptParser.BoolContext mockBoolContext;
    @Mock private MineScriptParser.AbsDirContext mockAbsDirContext;
    @Mock private MineScriptParser.RelDirContext mockRelDirContext;
    @Mock private MineScriptParser.NumberContext mockNumberContext;
    @Mock private MineScriptParser.IdContext mockIdContext;
    @Mock private MineScriptParser.NegContext mockNegContext;
    @Mock private MineScriptParser.NotExprContext mockNotExprContext;
    @Mock private MineScriptParser.AndContext mockAndContext;
    @Mock private MineScriptParser.AddSubContext mockAddSubContext;
    @Mock private MineScriptParser.OrContext mockOrContext;
    @Mock private MineScriptParser.MultDivModContext mockMultDivModContext;
    @Mock private MineScriptParser.IsIsNotContext mockIsIsNotContext;

    @ParameterizedTest
    @ValueSource(ints = {-1000, -10, 0, 10, 1000})
    void visitAssignStoresCorrectNumber(int value) {
        // Mock functions ID(), expression(), and visit()
        Mockito.when(mockAssignContext.ID()).thenReturn(new MockTerminalNode("varName"));
        Mockito.when(mockAssignContext.expression()).thenReturn(mockExpressionContext1);
        Mockito.when(spyVisitor.visit(mockExpressionContext1)).thenReturn(new MSNumber(value));

        // Call the visitAssign method on the spy
        MSType result = spyVisitor.visitAssign(mockAssignContext);

        // Initialize mock value and assert that no exceptions are thrown when value is retrieved
        AtomicReference<MSType> mockValue = new AtomicReference<>();
        Assertions.assertDoesNotThrow(() -> mockValue.set(symbolTable.retrieveSymbolValue(symbolTable.retrieveSymbol("varName"))));

        // Assert that the symbol value is equal to the initial assign value, and assert that result is null
        Assertions.assertEquals(value, ((MSNumber) mockValue.get()).getValue());
        Assertions.assertNull(result);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void visitAssignStoresCorrectBoolean(boolean value) {
        // Mock functions ID(), expression(), and visit()
        Mockito.when(mockAssignContext.ID()).thenReturn(new MockTerminalNode("varName"));
        Mockito.when(mockAssignContext.expression()).thenReturn(mockExpressionContext1);
        Mockito.when(spyVisitor.visit(mockExpressionContext1)).thenReturn(new MSBool(value));

        // Call the visitAssign method on the spy
        MSType result = spyVisitor.visitAssign(mockAssignContext);

        // Initialize mock value and assert that no exceptions are thrown when value is retrieved
        AtomicReference<MSType> mockValue = new AtomicReference<>();
        Assertions.assertDoesNotThrow(() -> mockValue.set(symbolTable.retrieveSymbolValue(symbolTable.retrieveSymbol("varName"))));

        // Assert that the symbol value is equal to the initial assign value, and assert that result is null
        Assertions.assertEquals(value, ((MSBool) mockValue.get()).getValue());
        Assertions.assertNull(result);
    }

    @ParameterizedTest
    @EnumSource(MSRelDir.Direction.class)
    void visitAssignStoresCorrectRelDir(MSRelDir.Direction value) {
        // Mock functions ID(), expression(), and visit()
        Mockito.when(mockAssignContext.ID()).thenReturn(new MockTerminalNode("varName"));
        Mockito.when(mockAssignContext.expression()).thenReturn(mockExpressionContext1);
        Mockito.when(spyVisitor.visit(mockExpressionContext1)).thenReturn(new MSRelDir(value.toString().toLowerCase()));

        // Call the visitAssign method on the spy
        MSType result = spyVisitor.visitAssign(mockAssignContext);

        // Initialize mock value and assert that no exceptions are thrown when value is retrieved
        AtomicReference<MSType> mockValue = new AtomicReference<>();
        Assertions.assertDoesNotThrow(() -> mockValue.set(symbolTable.retrieveSymbolValue(symbolTable.retrieveSymbol("varName"))));

        // Assert that the symbol value is equal to the initial assign value, and assert that result is null
        Assertions.assertEquals(value, ((MSRelDir) mockValue.get()).getValue());
        Assertions.assertNull(result);
    }

    @ParameterizedTest
    @EnumSource(MSAbsDir.Direction.class)
    void visitAssignStoresCorrectAbsDir(MSAbsDir.Direction value) {
        // Mock functions ID(), expression(), and visit()
        Mockito.when(mockAssignContext.ID()).thenReturn(new MockTerminalNode("varName"));
        Mockito.when(mockAssignContext.expression()).thenReturn(mockExpressionContext1);
        Mockito.when(spyVisitor.visit(mockExpressionContext1)).thenReturn(new MSAbsDir(value.toString().toLowerCase()));

        // Call the visitAssign method on the spy
        MSType result = spyVisitor.visitAssign(mockAssignContext);

        // Initialize mock value and assert that no exceptions are thrown when value is retrieved
        AtomicReference<MSType> mockValue = new AtomicReference<>();
        Assertions.assertDoesNotThrow(() -> mockValue.set(symbolTable.retrieveSymbolValue(symbolTable.retrieveSymbol("varName"))));

        // Assert that the symbol value is equal to the initial assign value, and assert that result is null
        Assertions.assertEquals(value, ((MSAbsDir) mockValue.get()).getValue());
        Assertions.assertNull(result);
    }

    @Test
    void visitNotExprValidBoolReturnsNegatedBool() {
        Mockito.when(mockNotExprContext.expression()).thenReturn(mockExpressionContext1);
        Mockito.when(spyVisitor.visit(mockExpressionContext1)).thenReturn(new MSBool(false));

        MSType result = spyVisitor.visitNotExpr(mockNotExprContext);
        Assertions.assertTrue(((MSBool) result).getValue());
    }

    @ParameterizedTest
    @ValueSource(ints = {-1000, -100, 0, 100, 1000})
    void visitNotExprPassNumberThrowsRuntimeException(int value) {
        Mockito.when(mockNotExprContext.expression()).thenReturn(mockExpressionContext);
        Mockito.when(spyVisitor.visit(mockExpressionContext)).thenReturn(new MSNumber(value));

        Assertions.assertThrows(RuntimeException.class, () -> spyVisitor.visitNotExpr(mockNotExprContext));
    }

    @Test
    void visitNotExprInvalidTypeThrowsRuntimeException() {
        Mockito.when(mockNotExprContext.expression()).thenReturn(mockExpressionContext1);
        Mockito.when(spyVisitor.visit(mockExpressionContext1)).thenReturn(new MSRelDir("right"));

        Assertions.assertThrows(RuntimeException.class, () -> spyVisitor.visitNotExpr(mockNotExprContext));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1000, -10, 0, 10, 1000})
    void visitNegNegatesNumber(int value) {
        Mockito.when(mockNegContext.expression()).thenReturn(mockExpressionContext1);
        Mockito.when(spyVisitor.visit(mockExpressionContext1)).thenReturn(new MSNumber(value));

        MSType result = spyVisitor.visitNeg(mockNegContext);
        Assertions.assertEquals(-value, ((MSNumber) result).getValue());
    }

    @Test
    void visitNegInvalidTypeThrowsRuntimeException() {
        Mockito.when(mockNegContext.expression()).thenReturn(mockExpressionContext1);
        Mockito.when(spyVisitor.visit(mockExpressionContext1)).thenReturn(new MSBool(false));

        Assertions.assertThrows(RuntimeException.class, () -> spyVisitor.visitNeg(mockNegContext));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1000, -10, 0, 10, 1000})
    void visitIdValidIdReturnsValue(int value) {
        Mockito.when(mockIdContext.ID()).thenReturn(new MockTerminalNode("varName"));
        symbolTable.enterSymbol("varName", new MSNumber(value));

        MSType result = spyVisitor.visitId(mockIdContext);
        Assertions.assertEquals(value, ((MSNumber) result).getValue());
    }

    @Test
    void visitIdInvalidIdThrowsRuntimeException() {
        Mockito.when(mockIdContext.ID()).thenReturn(new MockTerminalNode("varName"));
        Assertions.assertThrows(RuntimeException.class, () -> spyVisitor.visitId(mockIdContext));
    }

    @Test
    void visitBoolPassTrueReturnsTrue() {
        Mockito.when(mockBoolContext.getText()).thenReturn("true");

        MSType result = spyVisitor.visitBool(mockBoolContext);
        Assertions.assertTrue(((MSBool) result).getValue());
    }

    @Test
    void visitBoolPassFalseReturnsFalse() {
        Mockito.when(mockBoolContext.getText()).thenReturn("false");

        MSType result = spyVisitor.visitBool(mockBoolContext);
        Assertions.assertFalse(((MSBool) result).getValue());
    }

    @Test
    void visitBoolPassRandomStringReturnsFalse() {
        Mockito.when(mockBoolContext.getText()).thenReturn("abc");

        MSType result = spyVisitor.visitBool(mockBoolContext);
        Assertions.assertFalse(((MSBool) result).getValue());
    }

    @ParameterizedTest
    @ValueSource(strings = {"north", "south", "east", "west", "top", "bottom"})
    void visitAbsDirValidInputReturnsAbsDir(String value){
        Mockito.when(mockAbsDirContext.ABSDIR()).thenReturn(new MockTerminalNode(value));

        MSType result = spyVisitor.visitAbsDir(mockAbsDirContext);
        Assertions.assertEquals(new MSAbsDir(value).getValue(), ((MSAbsDir) result).getValue());
    }

    @Test
    void visitAbsDirInvalidInputThrowsIllegalArgumentException(){
        Mockito.when(mockAbsDirContext.ABSDIR()).thenReturn(new MockTerminalNode("abc"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> spyVisitor.visitAbsDir(mockAbsDirContext));
    }

    @ParameterizedTest
    @ValueSource(strings = {"up", "down", "left", "right"})
    void visitRelDirValidInputReturnsRelDir(String value) {
        Mockito.when(mockRelDirContext.RELDIR()).thenReturn(new MockTerminalNode(value));

        MSType result = spyVisitor.visitRelDir(mockRelDirContext);
        Assertions.assertEquals(new MSRelDir(value).getValue(), ((MSRelDir) result).getValue());
    }

    @Test
    void visitRelDirInvalidInputThrowsIllegalArgumentException(){
        Mockito.when(mockRelDirContext.RELDIR()).thenReturn(new MockTerminalNode("abc"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> spyVisitor.visitRelDir(mockRelDirContext));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1000, -10, 0, 10, 1000})
    void visitNumberValidInputReturnsNumber(int value) {
        Mockito.when(mockNumberContext.NUMBER()).thenReturn(new MockTerminalNode(String.valueOf(value)));

        MSType result = spyVisitor.visitNumber(mockNumberContext);
        Assertions.assertEquals(value, ((MSNumber) result).getValue());
    }

    @Test
    void visitNumberInvalidInputThrowsRuntimeException(){
        Mockito.when(mockNumberContext.NUMBER()).thenReturn(new MockTerminalNode("abc"));
        Assertions.assertThrows(RuntimeException.class, () -> spyVisitor.visitNumber(mockNumberContext));
    }
  
    @Test
    void visitAndValidBoolsReturnsTrue(){
        Mockito.when(mockAndContext.expression(0)).thenReturn(mockExpressionContext1);
        Mockito.when(mockAndContext.expression(1)).thenReturn(mockExpressionContext2);
        Mockito.when(spyVisitor.visit(mockExpressionContext1)).thenReturn(new MSBool(true));
        Mockito.when(spyVisitor.visit(mockExpressionContext2)).thenReturn(new MSBool(true));

        MSType result = spyVisitor.visitAnd(mockAndContext);
        Assertions.assertTrue(((MSBool) result).getValue());
    }
  
    @Test
    void visitAndBoolsTrueAndFalseReturnsFalse(){
        Mockito.when(mockAndContext.expression(0)).thenReturn(mockExpressionContext1);
        Mockito.when(mockAndContext.expression(1)).thenReturn(mockExpressionContext2);
        Mockito.when(spyVisitor.visit(mockExpressionContext1)).thenReturn(new MSBool(true));
        Mockito.when(spyVisitor.visit(mockExpressionContext2)).thenReturn(new MSBool(false));

        MSType result = spyVisitor.visitAnd(mockAndContext);
        Assertions.assertFalse(((MSBool) result).getValue());
    }
    @Test
    void visitAndFalseWithShortCircuitReturnsFalse() {
        Mockito.when(mockAndContext.expression(0)).thenReturn(mockExpressionContext1);
        Mockito.when(spyVisitor.visit(mockExpressionContext1)).thenReturn(new MSBool(false));

        MSType result = spyVisitor.visitAnd(mockAndContext);
        Assertions.assertFalse(((MSBool) result).getValue());
    }

    @ParameterizedTest
    @CsvSource ({"1, 2, +", "2, 1, -", "0, 0, +", "-1, -2, +", "-2, -1, -"})
    void visitAddSubValidInputReturnsNumber(int left, int right, String operator){
        mockAddSubContext.op = new MockToken(operator);
        Mockito.when(mockAddSubContext.expression(0)).thenReturn(mockExpressionContext1);
        Mockito.when(mockAddSubContext.expression(1)).thenReturn(mockExpressionContext2);
        Mockito.when(spyVisitor.visit(mockExpressionContext1)).thenReturn(new MSNumber(left));
        Mockito.when(spyVisitor.visit(mockExpressionContext2)).thenReturn(new MSNumber(right));

        MSType result = spyVisitor.visitAddSub(mockAddSubContext);
        if (operator.equals("+")){
            Assertions.assertEquals(left + right, ((MSNumber) result).getValue());
        } else if (operator.equals("-")){
            Assertions.assertEquals(left - right, ((MSNumber) result).getValue());
        }
    }

    @ParameterizedTest
    @CsvSource({"false, false", "false, true", "true, false", "true, true"})
    void visitOrValidInputReturnsOrValue(boolean left, boolean right) {
        Mockito.when(mockOrContext.expression(0)).thenReturn(mockExpressionContext1);
        Mockito.when(spyVisitor.visit(mockExpressionContext1)).thenReturn(new MSBool(left));

        if (!left) {
            Mockito.when(mockOrContext.expression(1)).thenReturn(mockExpressionContext2);
            Mockito.when(spyVisitor.visit(mockExpressionContext2)).thenReturn(new MSBool(right));
        }

        MSType result = spyVisitor.visitOr(mockOrContext);
        Assertions.assertEquals(left || right, ((MSBool) result).getValue());
    }

    @ParameterizedTest
    @CsvSource({"num,0,0,is", "num,0,1,is not", "num,1,0,is not", "num,1,1,is", "num,-1,1,is not",
            "rel,left,right,is", "rel,up,down,is not", "rel,left,left,is not", "rel,right,right,is",
            "abs,north,south,is", "abs,east,west,is not", "abs,top,bottom,is not", "abs,north,north,is",
            "bool,false,false,is", "bool,false,true,is not", "bool,true,false,is not", "bool,true,true,is"})
    void visitIsIsNotCorrectTypesReturnsCorrectBool(String type, String left, String right, String operator) {
        mockIsIsNotContext.op = new MockToken(operator);
        Mockito.when(mockIsIsNotContext.expression(0)).thenReturn(mockExpressionContext1);
        Mockito.when(mockIsIsNotContext.expression(1)).thenReturn(mockExpressionContext2);

        switch (type) {
            case "num" -> {
                Mockito.when(spyVisitor.visit(mockExpressionContext1)).thenReturn(new MSNumber(Integer.parseInt(left)));
                Mockito.when(spyVisitor.visit(mockExpressionContext2)).thenReturn(new MSNumber(Integer.parseInt(right)));
            }
            case "rel" -> {
                Mockito.when(spyVisitor.visit(mockExpressionContext1)).thenReturn(new MSRelDir(left));
                Mockito.when(spyVisitor.visit(mockExpressionContext2)).thenReturn(new MSRelDir(right));
            }
            case "abs" -> {
                Mockito.when(spyVisitor.visit(mockExpressionContext1)).thenReturn(new MSAbsDir(left));
                Mockito.when(spyVisitor.visit(mockExpressionContext2)).thenReturn(new MSAbsDir(right));
            }
            case "bool" -> {
                Mockito.when(spyVisitor.visit(mockExpressionContext1)).thenReturn(new MSBool(Boolean.parseBoolean(left)));
                Mockito.when(spyVisitor.visit(mockExpressionContext2)).thenReturn(new MSBool(Boolean.parseBoolean(right)));
            }
        }

        MSType result = spyVisitor.visitIsIsNot(mockIsIsNotContext);
        switch (operator) {
            case "is" -> Assertions.assertEquals(left.equals(right), ((MSBool) result).getValue());
            case "is not" -> Assertions.assertEquals(!left.equals(right), ((MSBool) result).getValue());
        }
    }

    @ParameterizedTest
    @CsvSource ({"2,2,*", "2,2,/", "2,2,%"})
    void visitMultDivModValidInputReturnsNumber(int left, int right, String operator) {
        mockMultDivModContext.op = new MockToken(operator);
        Mockito.when(mockMultDivModContext.expression(0)).thenReturn(mockExpressionContext1);
        Mockito.when(mockMultDivModContext.expression(1)).thenReturn(mockExpressionContext2);
        Mockito.when(spyVisitor.visit(mockExpressionContext1)).thenReturn(new MSNumber(left));
        Mockito.when(spyVisitor.visit(mockExpressionContext2)).thenReturn(new MSNumber(right));

        MSType result = spyVisitor.visitMultDivMod(mockMultDivModContext);
        switch (operator) {
            case "*" -> Assertions.assertEquals(left * right, ((MSNumber) result).getValue());
            case "/" -> Assertions.assertEquals(left / right, ((MSNumber) result).getValue());
            case "%" -> Assertions.assertEquals(left % right, ((MSNumber) result).getValue());
        }
    }

    @ParameterizedTest
    @CsvSource ({"2,0,/", "2,0,%"})
    void visitMultDivModDivByZeroThrowsRuntimeException(int left, int right, String operator) {
        mockMultDivModContext.op = new MockToken(operator);
        Mockito.when(mockMultDivModContext.expression(0)).thenReturn(mockExpressionContext1);
        Mockito.when(mockMultDivModContext.expression(1)).thenReturn(mockExpressionContext2);
        Mockito.when(spyVisitor.visit(mockExpressionContext1)).thenReturn(new MSNumber(left));
        Mockito.when(spyVisitor.visit(mockExpressionContext2)).thenReturn(new MSNumber(right));

        Assertions.assertThrows(RuntimeException.class, () -> spyVisitor.visitMultDivMod(mockMultDivModContext));
    }

    @ParameterizedTest
    @CsvSource ({"2,true,/", "2,true,%", "2,false,/", "2,false,%", "true,2,/", "true,2,%", "false,2,/", "false,2,%"})
    void visitMultDivModInvalidInputThrowsRuntimeException(String left, String right, String operator) {
        mockMultDivModContext.op = new MockToken(operator);
        Mockito.when(mockMultDivModContext.expression(0)).thenReturn(mockExpressionContext1);
        Mockito.when(mockMultDivModContext.expression(1)).thenReturn(mockExpressionContext2);
        Mockito.when(spyVisitor.visit(mockExpressionContext1)).thenReturn(new MSBool(Boolean.parseBoolean(left)));
        Mockito.when(spyVisitor.visit(mockExpressionContext2)).thenReturn(new MSBool(Boolean.parseBoolean(right)));

        Assertions.assertThrows(RuntimeException.class, () -> spyVisitor.visitMultDivMod(mockMultDivModContext));
    }
}
