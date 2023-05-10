package interpreter;

import interpreter.types.*;
import interpreter.utils.MockTerminalNode;
import interpreter.antlr.MineScriptParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.atomic.AtomicReference;

import static interpreter.utils.TreeFromString.getExprTreeFromString;

@ExtendWith(MockitoExtension.class)
class VisitorUnitTest {
    private final SymbolTable symbolTable = new SymbolTable();
    private final Visitor visitor = new Visitor(symbolTable);

    @Spy
    Visitor spyVisitor = visitor;
    @Mock
    MineScriptParser.AssignContext mockAssignContext;
    @Mock
    MineScriptParser.ExpressionContext mockExpressionContext;
    @Mock
    MineScriptParser.BoolContext mockBoolContext;
    @Mock
    MineScriptParser.AbsDirContext mockAbsDirContext;
    @Mock
    MineScriptParser.RelDirContext mockRelDirContext;

    @ParameterizedTest
    @ValueSource(ints = {-1000, -10, 0, 10, 1000})
    void visitAssignStoresCorrectNumbers(int value) {
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

    @Test
    void visitBoolPassTrueReturnsTrue() {
        Mockito.when(mockBoolContext.getText()).thenReturn("true");
        Assertions.assertTrue(((MSBool) visitor.visitBool(mockBoolContext)).getValue());
    }

    @Test
    void visitBoolPassFalseReturnsFalse() {
        Mockito.when(mockBoolContext.getText()).thenReturn("false");
        Assertions.assertFalse(((MSBool) visitor.visitBool(mockBoolContext)).getValue());
    }

    @Test
    void visitBoolPassStringReturnsFalse() {
        Mockito.when(mockBoolContext.getText()).thenReturn("abc");
        Assertions.assertFalse(((MSBool) visitor.visitBool(mockBoolContext)).getValue());
    }

    @ParameterizedTest
    @ValueSource(strings = {"north", "south", "east", "west", "top", "bottom"})
    void visitAbsDirWithCorrectInputsReturnsTrue(String value){
        Mockito.when(mockAbsDirContext.ABSDIR()).thenReturn(new MockTerminalNode(value));
        Assertions.assertEquals(((MSAbsDir) visitor.visitAbsDir(mockAbsDirContext)).getValue(), new MSAbsDir(value).getValue());
    }

    @ParameterizedTest
    @ValueSource(strings = {"up", "down", "left", "right"})
    void visitRelDirWithCorrectInputsReturnsTrue(String value) {
        Mockito.when(mockRelDirContext.RELDIR()).thenReturn(new MockTerminalNode(value));
        Assertions.assertEquals(((MSRelDir) visitor.visitRelDir(mockRelDirContext)).getValue(), new MSRelDir(value).getValue());
    }
}
