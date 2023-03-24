package interpreter.antlr;

// Generated from MineScript.g4 by ANTLR 4.12.0
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link MineScriptParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface MineScriptVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link MineScriptParser#program}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProgram(MineScriptParser.ProgramContext ctx);
	/**
	 * Visit a parse tree produced by {@link MineScriptParser#statements}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatements(MineScriptParser.StatementsContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Expr}
	 * labeled alternative in {@link MineScriptParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpr(MineScriptParser.ExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Assign}
	 * labeled alternative in {@link MineScriptParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssign(MineScriptParser.AssignContext ctx);
	/**
	 * Visit a parse tree produced by the {@code If}
	 * labeled alternative in {@link MineScriptParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIf(MineScriptParser.IfContext ctx);
	/**
	 * Visit a parse tree produced by the {@code While}
	 * labeled alternative in {@link MineScriptParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhile(MineScriptParser.WhileContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Repeat}
	 * labeled alternative in {@link MineScriptParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRepeat(MineScriptParser.RepeatContext ctx);
	/**
	 * Visit a parse tree produced by the {@code FuncDecl}
	 * labeled alternative in {@link MineScriptParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFuncDecl(MineScriptParser.FuncDeclContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Return}
	 * labeled alternative in {@link MineScriptParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReturn(MineScriptParser.ReturnContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Newline}
	 * labeled alternative in {@link MineScriptParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNewline(MineScriptParser.NewlineContext ctx);
	/**
	 * Visit a parse tree produced by the {@code IsIsNot}
	 * labeled alternative in {@link MineScriptParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIsIsNot(MineScriptParser.IsIsNotContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Or}
	 * labeled alternative in {@link MineScriptParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOr(MineScriptParser.OrContext ctx);
	/**
	 * Visit a parse tree produced by the {@code AddSub}
	 * labeled alternative in {@link MineScriptParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAddSub(MineScriptParser.AddSubContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Comp}
	 * labeled alternative in {@link MineScriptParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComp(MineScriptParser.CompContext ctx);
	/**
	 * Visit a parse tree produced by the {@code FuncCall}
	 * labeled alternative in {@link MineScriptParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFuncCall(MineScriptParser.FuncCallContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Neg}
	 * labeled alternative in {@link MineScriptParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNeg(MineScriptParser.NegContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Number}
	 * labeled alternative in {@link MineScriptParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumber(MineScriptParser.NumberContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Bool}
	 * labeled alternative in {@link MineScriptParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBool(MineScriptParser.BoolContext ctx);
	/**
	 * Visit a parse tree produced by the {@code MultDivMod}
	 * labeled alternative in {@link MineScriptParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMultDivMod(MineScriptParser.MultDivModContext ctx);
	/**
	 * Visit a parse tree produced by the {@code And}
	 * labeled alternative in {@link MineScriptParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnd(MineScriptParser.AndContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Block}
	 * labeled alternative in {@link MineScriptParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBlock(MineScriptParser.BlockContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Pow}
	 * labeled alternative in {@link MineScriptParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPow(MineScriptParser.PowContext ctx);
	/**
	 * Visit a parse tree produced by the {@code RelDir}
	 * labeled alternative in {@link MineScriptParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRelDir(MineScriptParser.RelDirContext ctx);
	/**
	 * Visit a parse tree produced by the {@code NotExpr}
	 * labeled alternative in {@link MineScriptParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNotExpr(MineScriptParser.NotExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Id}
	 * labeled alternative in {@link MineScriptParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitId(MineScriptParser.IdContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ParenExpr}
	 * labeled alternative in {@link MineScriptParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParenExpr(MineScriptParser.ParenExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code AbsDir}
	 * labeled alternative in {@link MineScriptParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAbsDir(MineScriptParser.AbsDirContext ctx);
	/**
	 * Visit a parse tree produced by {@link MineScriptParser#formal_paramaters}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFormal_paramaters(MineScriptParser.Formal_paramatersContext ctx);
	/**
	 * Visit a parse tree produced by {@link MineScriptParser#actual_parameters}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitActual_parameters(MineScriptParser.Actual_parametersContext ctx);
}