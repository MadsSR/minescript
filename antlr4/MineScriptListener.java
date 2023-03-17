// Generated from MineScript.g4 by ANTLR 4.12.0
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link MineScriptParser}.
 */
public interface MineScriptListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link MineScriptParser#program}.
	 * @param ctx the parse tree
	 */
	void enterProgram(MineScriptParser.ProgramContext ctx);
	/**
	 * Exit a parse tree produced by {@link MineScriptParser#program}.
	 * @param ctx the parse tree
	 */
	void exitProgram(MineScriptParser.ProgramContext ctx);
	/**
	 * Enter a parse tree produced by {@link MineScriptParser#statements}.
	 * @param ctx the parse tree
	 */
	void enterStatements(MineScriptParser.StatementsContext ctx);
	/**
	 * Exit a parse tree produced by {@link MineScriptParser#statements}.
	 * @param ctx the parse tree
	 */
	void exitStatements(MineScriptParser.StatementsContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Expr}
	 * labeled alternative in {@link MineScriptParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterExpr(MineScriptParser.ExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Expr}
	 * labeled alternative in {@link MineScriptParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitExpr(MineScriptParser.ExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Assign}
	 * labeled alternative in {@link MineScriptParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterAssign(MineScriptParser.AssignContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Assign}
	 * labeled alternative in {@link MineScriptParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitAssign(MineScriptParser.AssignContext ctx);
	/**
	 * Enter a parse tree produced by the {@code If}
	 * labeled alternative in {@link MineScriptParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterIf(MineScriptParser.IfContext ctx);
	/**
	 * Exit a parse tree produced by the {@code If}
	 * labeled alternative in {@link MineScriptParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitIf(MineScriptParser.IfContext ctx);
	/**
	 * Enter a parse tree produced by the {@code While}
	 * labeled alternative in {@link MineScriptParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterWhile(MineScriptParser.WhileContext ctx);
	/**
	 * Exit a parse tree produced by the {@code While}
	 * labeled alternative in {@link MineScriptParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitWhile(MineScriptParser.WhileContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Repeat}
	 * labeled alternative in {@link MineScriptParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterRepeat(MineScriptParser.RepeatContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Repeat}
	 * labeled alternative in {@link MineScriptParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitRepeat(MineScriptParser.RepeatContext ctx);
	/**
	 * Enter a parse tree produced by the {@code FuncDecl}
	 * labeled alternative in {@link MineScriptParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterFuncDecl(MineScriptParser.FuncDeclContext ctx);
	/**
	 * Exit a parse tree produced by the {@code FuncDecl}
	 * labeled alternative in {@link MineScriptParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitFuncDecl(MineScriptParser.FuncDeclContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Newline}
	 * labeled alternative in {@link MineScriptParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterNewline(MineScriptParser.NewlineContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Newline}
	 * labeled alternative in {@link MineScriptParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitNewline(MineScriptParser.NewlineContext ctx);
	/**
	 * Enter a parse tree produced by the {@code IsIsNot}
	 * labeled alternative in {@link MineScriptParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterIsIsNot(MineScriptParser.IsIsNotContext ctx);
	/**
	 * Exit a parse tree produced by the {@code IsIsNot}
	 * labeled alternative in {@link MineScriptParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitIsIsNot(MineScriptParser.IsIsNotContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Or}
	 * labeled alternative in {@link MineScriptParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterOr(MineScriptParser.OrContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Or}
	 * labeled alternative in {@link MineScriptParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitOr(MineScriptParser.OrContext ctx);
	/**
	 * Enter a parse tree produced by the {@code AddSub}
	 * labeled alternative in {@link MineScriptParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterAddSub(MineScriptParser.AddSubContext ctx);
	/**
	 * Exit a parse tree produced by the {@code AddSub}
	 * labeled alternative in {@link MineScriptParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitAddSub(MineScriptParser.AddSubContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Comp}
	 * labeled alternative in {@link MineScriptParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterComp(MineScriptParser.CompContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Comp}
	 * labeled alternative in {@link MineScriptParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitComp(MineScriptParser.CompContext ctx);
	/**
	 * Enter a parse tree produced by the {@code FuncCall}
	 * labeled alternative in {@link MineScriptParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterFuncCall(MineScriptParser.FuncCallContext ctx);
	/**
	 * Exit a parse tree produced by the {@code FuncCall}
	 * labeled alternative in {@link MineScriptParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitFuncCall(MineScriptParser.FuncCallContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Neg}
	 * labeled alternative in {@link MineScriptParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterNeg(MineScriptParser.NegContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Neg}
	 * labeled alternative in {@link MineScriptParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitNeg(MineScriptParser.NegContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Number}
	 * labeled alternative in {@link MineScriptParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterNumber(MineScriptParser.NumberContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Number}
	 * labeled alternative in {@link MineScriptParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitNumber(MineScriptParser.NumberContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Bool}
	 * labeled alternative in {@link MineScriptParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterBool(MineScriptParser.BoolContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Bool}
	 * labeled alternative in {@link MineScriptParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitBool(MineScriptParser.BoolContext ctx);
	/**
	 * Enter a parse tree produced by the {@code MultDivMod}
	 * labeled alternative in {@link MineScriptParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterMultDivMod(MineScriptParser.MultDivModContext ctx);
	/**
	 * Exit a parse tree produced by the {@code MultDivMod}
	 * labeled alternative in {@link MineScriptParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitMultDivMod(MineScriptParser.MultDivModContext ctx);
	/**
	 * Enter a parse tree produced by the {@code And}
	 * labeled alternative in {@link MineScriptParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterAnd(MineScriptParser.AndContext ctx);
	/**
	 * Exit a parse tree produced by the {@code And}
	 * labeled alternative in {@link MineScriptParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitAnd(MineScriptParser.AndContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Block}
	 * labeled alternative in {@link MineScriptParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterBlock(MineScriptParser.BlockContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Block}
	 * labeled alternative in {@link MineScriptParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitBlock(MineScriptParser.BlockContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Pow}
	 * labeled alternative in {@link MineScriptParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterPow(MineScriptParser.PowContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Pow}
	 * labeled alternative in {@link MineScriptParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitPow(MineScriptParser.PowContext ctx);
	/**
	 * Enter a parse tree produced by the {@code RelDir}
	 * labeled alternative in {@link MineScriptParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterRelDir(MineScriptParser.RelDirContext ctx);
	/**
	 * Exit a parse tree produced by the {@code RelDir}
	 * labeled alternative in {@link MineScriptParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitRelDir(MineScriptParser.RelDirContext ctx);
	/**
	 * Enter a parse tree produced by the {@code NotExpr}
	 * labeled alternative in {@link MineScriptParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterNotExpr(MineScriptParser.NotExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code NotExpr}
	 * labeled alternative in {@link MineScriptParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitNotExpr(MineScriptParser.NotExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Id}
	 * labeled alternative in {@link MineScriptParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterId(MineScriptParser.IdContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Id}
	 * labeled alternative in {@link MineScriptParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitId(MineScriptParser.IdContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ParenExpr}
	 * labeled alternative in {@link MineScriptParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterParenExpr(MineScriptParser.ParenExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ParenExpr}
	 * labeled alternative in {@link MineScriptParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitParenExpr(MineScriptParser.ParenExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code AbsDir}
	 * labeled alternative in {@link MineScriptParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterAbsDir(MineScriptParser.AbsDirContext ctx);
	/**
	 * Exit a parse tree produced by the {@code AbsDir}
	 * labeled alternative in {@link MineScriptParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitAbsDir(MineScriptParser.AbsDirContext ctx);
	/**
	 * Enter a parse tree produced by {@link MineScriptParser#formal_paramaters}.
	 * @param ctx the parse tree
	 */
	void enterFormal_paramaters(MineScriptParser.Formal_paramatersContext ctx);
	/**
	 * Exit a parse tree produced by {@link MineScriptParser#formal_paramaters}.
	 * @param ctx the parse tree
	 */
	void exitFormal_paramaters(MineScriptParser.Formal_paramatersContext ctx);
	/**
	 * Enter a parse tree produced by {@link MineScriptParser#actual_parameters}.
	 * @param ctx the parse tree
	 */
	void enterActual_parameters(MineScriptParser.Actual_parametersContext ctx);
	/**
	 * Exit a parse tree produced by {@link MineScriptParser#actual_parameters}.
	 * @param ctx the parse tree
	 */
	void exitActual_parameters(MineScriptParser.Actual_parametersContext ctx);
}