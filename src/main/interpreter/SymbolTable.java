package interpreter;

import interpreter.exceptions.SymbolNotFoundException;
import interpreter.types.MSFunction;
import interpreter.types.MSInbuiltFunction;
import interpreter.types.MSType;

import java.util.*;

public class SymbolTable {
    private final Map<String, Symbol> hashMap = new HashMap<>();
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

        /*If a prefixed version of the variable already exists, update it*/
        String shadowedSymbolName = getShadowedSymbolName(name);
        if (shadowedSymbolName != null) {
            Symbol oldSymbol = hashMap.get(shadowedSymbolName);
            delete(oldSymbol.name);
            Symbol newShadowedSymbol = new Symbol(oldSymbol.name, value);
            add(newShadowedSymbol);
            return;
        }

        /*If the variable is already in the symbol table, update it*/
        if (hashMap.containsKey(name)) {
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
        String shadowedSymbolName = getShadowedSymbolName(name);
        if (shadowedSymbolName != null) {
            return hashMap.get(shadowedSymbolName);
        } else if (hashMap.containsKey(name)) {
            return hashMap.get(name);
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
        hashMap.remove(name);
    }

    /**
     * @param symbol symbol to add to the symbol table
     */
    private void add(Symbol symbol) {
        hashMap.put(symbol.name, symbol);
    }

    /**
     * @param name id of the variable
     * @return name of the symbol if a prefixed version of it exists in any of the scopes, otherwise null
     */
    private String getShadowedSymbolName(String name) {
        if (scopeStack.empty()) return null;

        ArrayList<String> currentScope = scopeStack.peek();
        if (currentScope.stream().anyMatch(s -> s.endsWith("." + name))) {
            return currentScope.stream().filter(s -> s.endsWith("." + name)).findFirst().orElseThrow();
        }
        else {
            scopeStack.pop();
            String res = getShadowedSymbolName(name);
            scopeStack.push(currentScope);
            return res;
        }
    }

    private record Symbol(String name, MSType value) {
    }
}

