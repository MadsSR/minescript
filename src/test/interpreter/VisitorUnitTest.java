package interpreter;

import interpreter.types.MSBool;
import interpreter.utils.MockTerminalNode;
import interpreter.antlr.MineScriptParser;
import interpreter.types.MSNumber;
import interpreter.types.MSType;
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
    void visitBoolPassTrueExpectedTrue() {
        Mockito.when(mockBoolContext.getText()).thenReturn("true");
        Assertions.assertTrue(((MSBool) visitor.visitBool(mockBoolContext)).getValue());
    }

    @Test
    void visitBoolPassFalseExpectedFalse() {
        Mockito.when(mockBoolContext.getText()).thenReturn("false");
        Assertions.assertFalse(((MSBool) visitor.visitBool(mockBoolContext)).getValue());
    }
}
