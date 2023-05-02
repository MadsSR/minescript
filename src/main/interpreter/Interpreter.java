package interpreter;

import interpreter.types.MSMessageType;
import minescript.block.entity.TurtleBlockEntity;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import interpreter.antlr.*;

import java.util.Arrays;

public class Interpreter implements Runnable {

    private static TurtleBlockEntity entity;
    private static String program;

    public Interpreter(String program, TurtleBlockEntity entity) {
        Interpreter.program = program;
        Interpreter.entity = entity;
    }

    @Override
    public void run() {
        try {
            // create a CharStream that reads from standard input
    //        CharStream input = CharStreams.fromString(CharStreams.fromFileName("src/main/interpreter/input.minescript") + System.lineSeparator());
            CharStream input = CharStreams.fromString(program + System.lineSeparator());
            // create a lexer that feeds off of input CharStream
            MineScriptLexer lexer = new MineScriptLexer(input);
            lexer.removeErrorListeners();
            lexer.addErrorListener(InterpreterErrorListener.INSTANCE);

            // create a buffer of tokens pulled from the lexer
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            // create a parser that feeds off the tokens buffer
            MineScriptParser parser = new MineScriptParser(tokens);
            parser.removeErrorListeners();
            parser.addErrorListener(InterpreterErrorListener.INSTANCE);
            ParseTree tree = parser.program(); // begin parsing at init rule
            Visitor visitor = new Visitor(entity);
            visitor.visit(tree);
            //System.out.println(tree.toStringTree(parser)); // print LISP-style tree
        }
        catch (Exception e) {
            entity.print(e.getMessage(), MSMessageType.ERROR);
        }
    }
}