package interpreter;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import interpreter.antlr.*;

public class Interpreter {

    public static void main(String[] args) throws Exception {
        try {
            // create a CharStream that reads from standard input
            CharStream input = CharStreams.fromString(CharStreams.fromFileName("src/main/interpreter/input.minescript") + System.lineSeparator());
            // create a lexer that feeds off of input CharStream
            MineScriptLexer lexer = new MineScriptLexer(input);
            // create a buffer of tokens pulled from the lexer
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            // create a parser that feeds off the tokens buffer
            MineScriptParser parser = new MineScriptParser(tokens);
            ParseTree tree = parser.program(); // begin parsing at init rule
            Visitor visitor = new Visitor();
            visitor.visit(tree);
            System.out.println(tree.toStringTree(parser)); // print LISP-style tree
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }
}