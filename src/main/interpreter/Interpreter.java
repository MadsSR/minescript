package interpreter;

import interpreter.antlr.MineScriptLexer;
import interpreter.antlr.MineScriptParser;
import interpreter.exceptions.ThreadInterruptedException;
import interpreter.types.MSMessageType;
import minescript.network.TurtleCommands;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

public class Interpreter implements Runnable {
    private static MinecraftServer server;
    private static ServerWorld world;
    private static BlockPos turtlePos;
    private static String program;

    public Interpreter(String program, MinecraftServer server, ServerWorld world, BlockPos pos) {
        Interpreter.program = program;
        Interpreter.server = server;
        Interpreter.world = world;
        Interpreter.turtlePos = pos;
    }

    @Override
    public void run() {
        try {
            System.out.println("Thread started");
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
            Visitor visitor = new Visitor(server, world, turtlePos, new SymbolTable());
            visitor.visit(tree);
        } catch (ThreadInterruptedException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            TurtleCommands.print(server, e.getMessage(), MSMessageType.ERROR);
        }
    }
}