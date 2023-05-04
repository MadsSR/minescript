package interpreter;

import interpreter.exceptions.SymbolNotFoundException;
import interpreter.types.MSTypeEnum;
import interpreter.types.MSType;

import java.util.*;

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

    public void enterSymbol(String name, MSTypeEnum type, MSType value) {
        Symbol newSymbol = new Symbol(name, type, value);

        if (isVarInNewScope(name)) {
            Symbol oldSymbol = hashTable.get(getPrefixName(name));
            delete(oldSymbol.name);
            Symbol prefixSymbol = new Symbol(oldSymbol.name, type, value);
            add(prefixSymbol);
            return;
        }

        if (hashTable.containsKey(name)) {
            delete(newSymbol.name);
        } else {
            scopeStack.peek().add(newSymbol.name);
        }
        add(newSymbol);
    }

    public Symbol retrieveSymbol(String name) {
        if (isVarInNewScope(name)) {
            return hashTable.get(getPrefixName(name));
        } else if (hashTable.containsKey(name)) {
            return hashTable.get(name);
        } else {
            throw new SymbolNotFoundException("Could not find symbol in symbol table: " + name);
        }
    }

    public MSType retrieveSymbolValue(Symbol symbol) {
        return symbol.value;
    }

    private void delete(String name) {
        hashTable.remove(name);
    }

    private void add(Symbol symbol) {
        hashTable.put(symbol.name, symbol);
    }

    /**
     * @param name name of the variable
     * @return true if the variable is in the current scope
     */
    private boolean isVarInNewScope(String name) {
        return scopeStack.peek().stream().anyMatch(s -> s.endsWith("." + name));
    }

    private String getPrefixName(String name) {
        return scopeStack.peek().stream().filter(s -> s.contains("." + name)).findFirst().orElseThrow();
    }

    private static class Symbol {
        final String name;
        final MSTypeEnum type;
        final MSType value;

        public Symbol(String name, MSTypeEnum type, MSType value) {
            this.name = name;
            this.type = type;
            this.value = value;
        }
    }
}

