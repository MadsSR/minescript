package src.main.interpreter;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private int depth = 0;
    private final Map<String, Symbol> hashTable = new HashMap<>();
    private final ArrayList<ArrayList<Symbol>> scopeDisplay = new ArrayList<>();

    public SymbolTable() {
        scopeDisplay.add(new ArrayList<>());
    }

    public void enterScope() {
        depth++;

        if (scopeDisplay.size() <= depth)
            scopeDisplay.add(new ArrayList<>());
    }

    public void exitScope() {
        for (Symbol symbol : scopeDisplay.get(depth)) {
            delete(symbol.name);
        }
        scopeDisplay.get(depth).clear();
        depth--;
    }

    public void enterSymbol(String name, Type type, Object value) {
        Symbol newSymbol = new Symbol(name, type, value);

        if (hashTable.containsKey(name)) {
            delete(newSymbol.name);
        }
        else {
            scopeDisplay.get(depth).add(newSymbol);
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

