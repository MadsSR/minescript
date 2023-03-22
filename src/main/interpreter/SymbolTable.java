package src.main.interpreter;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SymbolTable {
    private int depth = 0;
    private Map<String, Symbol> hashTable = new HashMap<>();
    private ArrayList<ArrayList<Symbol>> scopeDisplay = new ArrayList();

    public SymbolTable() {
        scopeDisplay.add(new ArrayList());
    }

    public void enterScope() {
        depth++;

        if (scopeDisplay.size() <= depth)
            scopeDisplay.add(new ArrayList());

        scopeDisplay.get(depth).clear();
    }

    public void exitScope() {
        for(Symbol sym : scopeDisplay.get(depth)) {
            Symbol prevSym = sym.var;
            delete(sym.name);
            if (prevSym != null) {
                add(sym.name, prevSym);
            }
        }
        depth--;
    }

    public void enterSymbol(String name, Type type, Object value) throws Exception {
        Symbol oldSym = retrieveSymbol(name);
//        if (oldSym != null && oldSym.depth == depth) {
//            throw new Exception("Symbol already exists");
//        }
        Symbol newSym = new Symbol(name, type);

        // Add to scope display
        //newSym.level = scopeDisplay.get(depth);
        newSym.depth = depth;
        newSym.value = value;

        if (scopeDisplay.get(depth).contains(oldSym)) {
            scopeDisplay.get(depth).remove(oldSym);
        }
        scopeDisplay.get(depth).add(newSym);



        // Add to hash table
        if (oldSym == null) {
            add(name, newSym);
        }
        else {
            delete(oldSym.name);
            add(name, newSym);
        }
        newSym.var = oldSym;
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

    private void add(String name, Symbol symbol) {
        hashTable.put(name, symbol);
    }

    private class Symbol {
        String name;
        Type type;
        Symbol var;
        Object value;
        Symbol level;
        int depth;

        public Symbol(String name, Type type) {
            this.name = name;
            this.type = type;
        }
    }
}

