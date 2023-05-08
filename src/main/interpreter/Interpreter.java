package interpreter;

import interpreter.antlr.MineScriptLexer;
import interpreter.antlr.MineScriptParser;
import interpreter.types.MSMessageType;
import minescript.block.entity.TurtleBlockEntity;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

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
            // Create a CharStream that reads from standard input
            CharStream input = CharStreams.fromString(program + System.lineSeparator());
            // Create a lexer that feeds off of input CharStream
            MineScriptLexer lexer = new MineScriptLexer(input);
            lexer.removeErrorListeners();
            lexer.addErrorListener(InterpreterErrorListener.INSTANCE);
            // Create a buffer of tokens pulled from the lexer
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            // Create a parser that feeds off the tokens buffer
            MineScriptParser parser = new MineScriptParser(tokens);
            parser.removeErrorListeners();
            parser.addErrorListener(InterpreterErrorListener.INSTANCE);
            ParseTree tree = parser.program(); // Begin parsing at init rule
            Visitor visitor = new Visitor(entity);
            visitor.visit(tree);
        } catch (Exception e) {
            entity.print(e.getMessage(), MSMessageType.ERROR);
        }
    }
}