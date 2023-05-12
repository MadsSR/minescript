package interpreter;

import interpreter.exceptions.SymbolNotFoundException;
import interpreter.types.MSFunction;
import interpreter.types.MSInbuiltFunction;
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
        /*Removes everything in the current scope from the hashtable*/
        for (String symbolName : scopeStack.peek()) {
            delete(symbolName);
        }
        scopeStack.pop();
    }

    /**
     * @param name id of symbol to enter
     * @param value value of symbol to enter
     */
    public void enterSymbol(String name, MSType value) {
        Symbol newSymbol = new Symbol(name, value);
        checkRestrictedKeyWords(newSymbol);

        /*If the variable is already in the current scope, update it*/
        if (isVarInNewScope(name)) {
            Symbol oldSymbol = hashTable.get(getPrefixName(name));
            delete(oldSymbol.name);
            Symbol prefixSymbol = new Symbol(oldSymbol.name, value);
            add(prefixSymbol);
            return;
        }

        /*If the variable is already in the symbol table, update it*/
        if (hashTable.containsKey(name)) {
            delete(newSymbol.name);
        } else {
            scopeStack.peek().add(newSymbol.name);
        }
        add(newSymbol);
    }

    /**
     * @param symbol symbol to check for restricted keywords
     */
    private void checkRestrictedKeyWords(Symbol symbol) {
        for (MSInbuiltFunction funcName : MSInbuiltFunction.values()) {
            if (symbol.value instanceof MSFunction f) {
                if (symbol.name.equals(funcName.name())) {
                    throw new RuntimeException("Cannot declare function with restricted name: " + funcName.name());
                }
                else if (f.getParameters().stream().anyMatch(p -> p.equals(funcName.name()))) {
                    throw new RuntimeException("Cannot declare function with restricted parameter name: " + funcName.name());
                }
            }
            else if (symbol.name.equals(funcName.name())){
                throw new RuntimeException("Cannot declare variable with restricted name: " + funcName.name());
            }
        }
    }

    /**
     * @param name id of the variable
     * @return symbol from the hash table
     */
    public Symbol retrieveSymbol(String name) {
        if (isVarInNewScope(name)) {
            return hashTable.get(getPrefixName(name));
        } else if (hashTable.containsKey(name)) {
            return hashTable.get(name);
        } else {
            throw new SymbolNotFoundException("Could not find symbol in symbol table: " + name);
        }
    }

    /**
     * @param symbol symbol to retrieve value of
     * @return value of the symbol
     */
    public MSType retrieveSymbolValue(Symbol symbol) {
        return symbol.value;
    }

    /**
     * @param name id of the variable
     */
    private void delete(String name) {
        hashTable.remove(name);
    }

    /**
     * @param symbol symbol to add to the symbol table
     */
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

    /**
     * @param name id of the variable
     * @return prefix of the variable
     */
    private String getPrefixName(String name) {
        return scopeStack.peek().stream().filter(s -> s.contains("." + name)).findFirst().orElseThrow();
    }

    private record Symbol(String name, MSType value) {
    }
}

