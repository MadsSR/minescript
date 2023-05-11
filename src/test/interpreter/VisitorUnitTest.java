package interpreter;

import interpreter.types.*;
import interpreter.utils.MockTerminalNode;
import interpreter.antlr.MineScriptParser;
import net.minecraft.block.*;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.atomic.AtomicReference;

import static interpreter.utils.TreeFromString.getExprTreeFromString;

@ExtendWith(MockitoExtension.class)
class VisitorUnitTest {
    private final SymbolTable symbolTable = new SymbolTable();
    private final Visitor visitor = new Visitor(symbolTable);

    @Spy private final Visitor spyVisitor = visitor;
    @Mock private MineScriptParser.AssignContext mockAssignContext;
    @Mock private MineScriptParser.ExpressionContext mockExpressionContext;
    @Mock private MineScriptParser.BoolContext mockBoolContext;
    @Mock private MineScriptParser.AbsDirContext mockAbsDirContext;
    @Mock private MineScriptParser.RelDirContext mockRelDirContext;
    @Mock private MineScriptParser.NumberContext mockNumberContext;
    @Mock private MineScriptParser.IdContext mockIdContext;
    @Mock private MineScriptParser.NegContext mockNegContext;

    @ParameterizedTest
    @ValueSource(ints = {-1000, -10, 0, 10, 1000})
    void visitAssignStoresCorrectNumber(int value) {
        // Mock functions ID(), expression(), and visit()
        Mockito.when(mockAssignContext.ID()).thenReturn(new MockTerminalNode("varName"));
        Mockito.when(mockAssignContext.expression()).thenReturn(mockExpressionContext);
        Mockito.when(spyVisitor.visit(mockExpressionContext)).thenReturn(new MSNumber(value));

        // Call the visitAssign method on the spy
        MSType result = spyVisitor.visitAssign(mockAssignContext);

        // Initialize mock value and assert that no exceptions are thrown when value is retrieved
        AtomicReference<MSType> mockValue = new AtomicReference<>();
        Assertions.assertDoesNotThrow(() -> mockValue.set(symbolTable.retrieveSymbolValue(symbolTable.retrieveSymbol("varName"))));

        // Assert that the symbol value is equal to the initial assign value, and assert that result is null
        Assertions.assertTrue(mockValue.get().equals(new MSNumber(value)));
        Assertions.assertNull(result);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1000, -10, 0, 10, 1000})
    void visitNegNegatesNumber(int value) {
        Mockito.when(mockNegContext.expression()).thenReturn(mockExpressionContext);
        Mockito.when(spyVisitor.visit(mockExpressionContext)).thenReturn(new MSNumber(value));

        MSType result = spyVisitor.visitNeg(mockNegContext);
        Assertions.assertTrue(result.equals(new MSNumber(-value)));
    }

    @Test
    void visitNegInvalidTypeThrowsRuntimeException() {
        Mockito.when(mockNegContext.expression()).thenReturn(mockExpressionContext);
        Mockito.when(spyVisitor.visit(mockExpressionContext)).thenReturn(new MSBool(false));

        Assertions.assertThrows(RuntimeException.class, () -> spyVisitor.visitNeg(mockNegContext));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1000, -10, 0, 10, 1000})
    void visitIdValidIdReturnsValue(int value) {
        Mockito.when(mockIdContext.ID()).thenReturn(new MockTerminalNode("varName"));
        symbolTable.enterSymbol("varName", new MSNumber(value));

        MSType result = spyVisitor.visitId(mockIdContext);
        Assertions.assertTrue(result.equals(new MSNumber(value)));
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
        Assertions.assertTrue(result.equals(new MSAbsDir(value)));
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
        Assertions.assertTrue(result.equals(new MSRelDir(value)));
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
        Assertions.assertTrue(result.equals(new MSNumber(value)));
    }

    @Test
    void visitNumberInvalidInputThrowsIllegalArgumentException(){
        Mockito.when(mockNumberContext.NUMBER()).thenReturn(new MockTerminalNode("abc"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> spyVisitor.visitNumber(mockNumberContext));
    }
}
