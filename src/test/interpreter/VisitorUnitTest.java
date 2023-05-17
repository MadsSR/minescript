package interpreter;

import interpreter.antlr.MineScriptParser;
import interpreter.types.*;
import interpreter.utils.MockTerminalNode;
import interpreter.utils.MockToken;
import org.antlr.v4.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

@ExtendWith(MockitoExtension.class)
class VisitorUnitTest {
    private final SymbolTable symbolTable = new SymbolTable();
    private final Visitor visitor = new Visitor(symbolTable);

    @Spy private final Visitor spyVisitor = visitor;
    @Mock private MineScriptParser.AssignContext mockAssignContext;
    @Mock private MineScriptParser.ExpressionContext mockExpressionContext1;
    @Mock private MineScriptParser.ExpressionContext mockExpressionContext2;
    @Mock private MineScriptParser.BoolContext mockBoolContext;
    @Mock private MineScriptParser.AbsDirContext mockAbsDirContext;
    @Mock private MineScriptParser.RelDirContext mockRelDirContext;
    @Mock private MineScriptParser.NumberContext mockNumberContext;
    @Mock private MineScriptParser.IdContext mockIdContext;
    @Mock private MineScriptParser.NegContext mockNegContext;
    @Mock private MineScriptParser.NotExprContext mockNotExprContext;
    @Mock private MineScriptParser.AddSubContext mockAddSubContext;
    @Mock private MineScriptParser.PowContext mockPowContext;
    @Mock private MineScriptParser.ParenExprContext mockParenExprContext;
    @Mock private MineScriptParser.CompContext mockCompContext;
    @Mock private MineScriptParser.FuncCallContext mockFuncCallContext;
    @Mock private MineScriptParser.Actual_parametersContext mockActualParametersContext;
    @Mock private MineScriptParser.StatementsContext mockStatementsContext;



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

    @Test
    void visitNotExprValidBoolReturnsNegatedBool() {
        Mockito.when(mockNotExprContext.expression()).thenReturn(mockExpressionContext1);
        Mockito.when(spyVisitor.visit(mockExpressionContext1)).thenReturn(new MSBool(false));

        MSType result = spyVisitor.visitNotExpr(mockNotExprContext);
        Assertions.assertTrue(((MSBool) result).getValue());
    }

    @Test
    void visitNotExprPassZeroReturnsTrue() {
        Mockito.when(mockNotExprContext.expression()).thenReturn(mockExpressionContext1);
        Mockito.when(spyVisitor.visit(mockExpressionContext1)).thenReturn(new MSNumber(0));

        MSType result = spyVisitor.visitNotExpr(mockNotExprContext);
        Assertions.assertTrue(((MSBool) result).getValue());
    }

    @ParameterizedTest
    @ValueSource(ints = {-1000, -100, 100, 1000})
    void visitNotExprPassNonZeroNumberReturnsFalse(int value) {
        Mockito.when(mockNotExprContext.expression()).thenReturn(mockExpressionContext1);
        Mockito.when(spyVisitor.visit(mockExpressionContext1)).thenReturn(new MSNumber(value));

        MSType result = spyVisitor.visitNotExpr(mockNotExprContext);
        Assertions.assertFalse(((MSBool) result).getValue());
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

    @ParameterizedTest
    @CsvSource ({"1, 2, +", "2, 1, -", "0, 0, +", "-1, -2, +", "-2, -1, -"})
    void visitAddSubValidInputReturnsNumber(int value1, int value2, String operator){
        mockAddSubContext.op = new MockToken(operator);
        Mockito.when(mockAddSubContext.expression(0)).thenReturn(mockExpressionContext1);
        Mockito.when(mockAddSubContext.expression(1)).thenReturn(mockExpressionContext2);
        Mockito.when(spyVisitor.visit(mockExpressionContext1)).thenReturn(new MSNumber(value1));
        Mockito.when(spyVisitor.visit(mockExpressionContext2)).thenReturn(new MSNumber(value2));

        MSType result = spyVisitor.visitAddSub(mockAddSubContext);
        if (operator.equals("+")){
            Assertions.assertEquals(value1 + value2, ((MSNumber) result).getValue());
        } else if (operator.equals("-")){
            Assertions.assertEquals(value1 - value2, ((MSNumber) result).getValue());
        }
    }

    @Test
    void visitAddSubInvalidTypeThrowsRuntimeException(){
        mockAddSubContext.op = new MockToken("+");
        Mockito.when(mockAddSubContext.expression(0)).thenReturn(mockExpressionContext1);
        Mockito.when(mockAddSubContext.expression(1)).thenReturn(mockExpressionContext2);
        Mockito.when(spyVisitor.visit(mockExpressionContext1)).thenReturn(new MSBool(false));
        Mockito.when(spyVisitor.visit(mockExpressionContext2)).thenReturn(new MSNumber(2));

        Assertions.assertThrows(RuntimeException.class, () -> spyVisitor.visitAddSub(mockAddSubContext));
    }

    @ParameterizedTest
    @CsvSource ({"1, 2", "2, 1", "0, 0", "-1, 1", "-2, 2"})
    void visitPowValidInputReturnsNumber(int value1, int value2){
        Mockito.when(mockPowContext.expression(0)).thenReturn(mockExpressionContext1);
        Mockito.when(mockPowContext.expression(1)).thenReturn(mockExpressionContext2);
        Mockito.when(spyVisitor.visit(mockExpressionContext1)).thenReturn(new MSNumber(value1));
        Mockito.when(spyVisitor.visit(mockExpressionContext2)).thenReturn(new MSNumber(value2));

        MSType result = spyVisitor.visitPow(mockPowContext);
        Assertions.assertEquals(Math.pow(value1, value2), ((MSNumber) result).getValue());
    }

    @Test
    void visitPowInvalidInputThrowsRuntimeException(){
        Mockito.when(mockPowContext.expression(0)).thenReturn(mockExpressionContext1);
        Mockito.when(mockPowContext.expression(1)).thenReturn(mockExpressionContext2);
        Mockito.when(spyVisitor.visit(mockExpressionContext1)).thenReturn(new MSNumber(1));
        Mockito.when(spyVisitor.visit(mockExpressionContext2)).thenReturn(new MSNumber(-1));

        Assertions.assertThrows(RuntimeException.class, () -> spyVisitor.visitPow(mockPowContext));
    }

    @ParameterizedTest
    @CsvSource ({"true, false", "false, false"})
    void visitPowInvalidTypeThrowsRuntimeException(boolean bool1, boolean bool2){
        Mockito.when(mockPowContext.expression(0)).thenReturn(mockExpressionContext1);
        Mockito.when(mockPowContext.expression(1)).thenReturn(mockExpressionContext2);
        Mockito.when(spyVisitor.visit(mockExpressionContext1)).thenReturn(new MSBool(bool1));
        Mockito.when(spyVisitor.visit(mockExpressionContext2)).thenReturn(new MSBool(bool2));

        Assertions.assertThrows(RuntimeException.class, () -> spyVisitor.visitPow(mockPowContext));

    }

    @ParameterizedTest
    @CsvSource ({"1", "2", "0", "-1", "-2"})
    void visitParenExprValidInputReturnsNumber(int value){
        Mockito.when(mockParenExprContext.expression()).thenReturn(mockExpressionContext1);
        Mockito.when(spyVisitor.visit(mockExpressionContext1)).thenReturn(new MSNumber(value));

        MSType result = spyVisitor.visitParenExpr(mockParenExprContext);
        Assertions.assertEquals(value, ((MSNumber) result).getValue());
    }

    @ParameterizedTest
    @ValueSource (booleans = {true, false})
    void visitParenExprValidInputReturnsBoolean(boolean value){
        Mockito.when(mockParenExprContext.expression()).thenReturn(mockExpressionContext1);
        Mockito.when(spyVisitor.visit(mockExpressionContext1)).thenReturn(new MSBool(value));

        MSType result = spyVisitor.visitParenExpr(mockParenExprContext);
        Assertions.assertEquals(value, ((MSBool) result).getValue());
    }

    @ParameterizedTest
    @CsvSource ({"1, 2, <", "2, 1, >", "0, 0, >=", "-1, 1, <", "-2, 2, <="})
    void visitCompValidInputReturnsTrue(int value1, int value2, String operator){
        mockCompContext.op = new MockToken(operator);
        Mockito.when(mockCompContext.expression(0)).thenReturn(mockExpressionContext1);
        Mockito.when(mockCompContext.expression(1)).thenReturn(mockExpressionContext2);
        Mockito.when(spyVisitor.visit(mockExpressionContext1)).thenReturn(new MSNumber(value1));
        Mockito.when(spyVisitor.visit(mockExpressionContext2)).thenReturn(new MSNumber(value2));

        MSType result = spyVisitor.visitComp(mockCompContext);
        Assertions.assertEquals(true, ((MSBool) result).getValue());
    }

    @ParameterizedTest
    @CsvSource ({"1, 2, >=", "2, 1, <", "0, 0, <", "-1, 1, >=", "-2, 2, >"})
    void visitCompValidInputReturnsFalse(int value1, int value2, String operator){
        mockCompContext.op = new MockToken(operator);
        Mockito.when(mockCompContext.expression(0)).thenReturn(mockExpressionContext1);
        Mockito.when(mockCompContext.expression(1)).thenReturn(mockExpressionContext2);
        Mockito.when(spyVisitor.visit(mockExpressionContext1)).thenReturn(new MSNumber(value1));
        Mockito.when(spyVisitor.visit(mockExpressionContext2)).thenReturn(new MSNumber(value2));

        MSType result = spyVisitor.visitComp(mockCompContext);
        Assertions.assertEquals(false, ((MSBool) result).getValue());
    }

    @Test
    void visitCompInvalidTypeThrowsException(){
        mockCompContext.op = new MockToken(">");
        Mockito.when(mockCompContext.expression(0)).thenReturn(mockExpressionContext1);
        Mockito.when(mockCompContext.expression(1)).thenReturn(mockExpressionContext2);
        Mockito.when(spyVisitor.visit(mockExpressionContext1)).thenReturn(new MSBool(true));
        Mockito.when(spyVisitor.visit(mockExpressionContext2)).thenReturn(new MSNumber(14));

        Assertions.assertThrows(RuntimeException.class, () -> spyVisitor.visitComp(mockCompContext));
    }

    @Test
    void visitFuncCallValidInputNoParamsReturnsNumber(){
        Mockito.when(mockFuncCallContext.actual_parameters()).thenReturn(mockActualParametersContext);
        Mockito.when(mockFuncCallContext.ID()).thenReturn(new MockTerminalNode("testFunction"));
        symbolTable.enterSymbol("testFunction", new MSFunction("testFunction", new ArrayList<String>(), mockStatementsContext));

        Mockito.when(spyVisitor.visit(mockStatementsContext)).thenReturn(new MSNumber(1));

        MSType result = spyVisitor.visitFuncCall(mockFuncCallContext);
        Assertions.assertEquals(1, ((MSNumber) result).getValue());
    }
    @ParameterizedTest
    @CsvSource ({"1", "2", "0", "-1", "-2", "123", "-123"})
    void visitFuncCallValidInputOneParamReturnsNumber(int value){

        Mockito.when(mockFuncCallContext.ID()).thenReturn(new MockTerminalNode("testFunction"));
        Mockito.when(mockFuncCallContext.actual_parameters()).thenReturn(mockActualParametersContext);
        Mockito.when(mockFuncCallContext.actual_parameters().expression()).thenReturn(new ArrayList<MineScriptParser.ExpressionContext>(){{add(mockExpressionContext1);}});
        Mockito.when(spyVisitor.visit(mockExpressionContext1)).thenReturn(new MSNumber(value));

        symbolTable.enterSymbol("testFunction", new MSFunction("testFunction", new ArrayList<String>(){{add("Param1");}}, mockStatementsContext));

        Mockito.when(spyVisitor.visit(mockStatementsContext)).thenReturn(new MSNumber(value));

        MSType result = spyVisitor.visitFuncCall(mockFuncCallContext);
        Assertions.assertEquals(value, ((MSNumber) result).getValue());
    }

    @ParameterizedTest
    @CsvSource ({"1, 2", "2, 1", "0, 0", "-1, 1", "-2, 2", "123, 122", "123, 124", "123, 123"})
    void visitFuncCallValidInputTwoParamReturnsNumber(int value1, int value2){

        Mockito.when(mockFuncCallContext.ID()).thenReturn(new MockTerminalNode("testFunction"));
        Mockito.when(mockFuncCallContext.actual_parameters()).thenReturn(mockActualParametersContext);
        Mockito.when(mockFuncCallContext.actual_parameters().expression()).thenReturn(new ArrayList<MineScriptParser.ExpressionContext>(){{add(mockExpressionContext1); add(mockExpressionContext2);}});
        Mockito.when(spyVisitor.visit(mockExpressionContext1)).thenReturn(new MSNumber(value1));
        Mockito.when(spyVisitor.visit(mockExpressionContext2)).thenReturn(new MSNumber(value2));

        symbolTable.enterSymbol("testFunction", new MSFunction("testFunction", new ArrayList<String>(){{add("Param1"); add("Param2");}}, mockStatementsContext));

        Mockito.when(spyVisitor.visit(mockStatementsContext)).thenReturn(new MSNumber(value1+value2));

        MSType result = spyVisitor.visitFuncCall(mockFuncCallContext);
        Assertions.assertEquals(value1+value2, ((MSNumber) result).getValue());
    }

    @ParameterizedTest
    @CsvSource ({"1, 2", "2, 1", "0, 0", "-1, 1", "-2, 2"})
    void visitFuncCallInvalidInputThrowsException(int value1, int value2){

        Mockito.when(mockFuncCallContext.ID()).thenReturn(new MockTerminalNode("testFunction"));
        Mockito.when(mockFuncCallContext.actual_parameters()).thenReturn(mockActualParametersContext);
        Mockito.when(mockFuncCallContext.actual_parameters().expression()).thenReturn(new ArrayList<MineScriptParser.ExpressionContext>(){{add(mockExpressionContext1); add(mockExpressionContext2);}});
        Mockito.when(spyVisitor.visit(mockExpressionContext1)).thenReturn(new MSNumber(value1));
        Mockito.when(spyVisitor.visit(mockExpressionContext2)).thenReturn(new MSNumber(value2));

        symbolTable.enterSymbol("testFunction", new MSFunction("testFunction", new ArrayList<String>(){{add("Param1");}}, mockStatementsContext));

        Assertions.assertThrows(RuntimeException.class, () -> spyVisitor.visitFuncCall(mockFuncCallContext));
    }

    @Test
    void visitFuncCallInvalidFunction(){
        Mockito.when(mockFuncCallContext.ID()).thenReturn(new MockTerminalNode("testFunctionFake"));
        Mockito.when(mockFuncCallContext.actual_parameters()).thenReturn(mockActualParametersContext);
        symbolTable.enterSymbol("testFunction", new MSFunction("testFunction", new ArrayList<String>(){{add("Param1"); }}, mockStatementsContext));

        Assertions.assertThrows(RuntimeException.class, () -> spyVisitor.visitFuncCall(mockFuncCallContext));
    }




}
