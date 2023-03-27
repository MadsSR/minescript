package interpreter;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class SymbolTable {
    private final Map<String, Symbol> hashTable = new HashMap<>();
    private final Stack<ArrayList<String>> scopeStack = new Stack<>();

    public SymbolTable() {
        scopeStack.push(new ArrayList<>());
    }

    public void enterScope() {
        scopeStack.push(new ArrayList<>());
    }

    public void exitScope() {
        for (String symbolName : scopeStack.peek()) {
            delete(symbolName);
        }
        scopeStack.pop();
    }

    public void enterSymbol(String name, Type type, Object value) {
        Symbol newSymbol = new Symbol(name, type, value);

        if (hashTable.containsKey(name)) {
            delete(newSymbol.name);
        }
        else {
            scopeStack.peek().add(newSymbol.name);
        }
        add(newSymbol);
    }

    public Symbol retrieveSymbol(String name) {
        return hashTable.get(name);
    }
    public Object retrieveSymbolValue(Symbol symbol) {
        return symbol.value;
    }

    private void delete(String name) {
        hashTable.remove(name);
    }

    private void add(Symbol symbol) {
        hashTable.put(symbol.name, symbol);
    }

    private static class Symbol {
        String name;
        Type type;
        Object value;

        public Symbol(String name, Type type, Object value) {
            this.name = name;
            this.type = type;
            this.value = value;
        }
    }
}

