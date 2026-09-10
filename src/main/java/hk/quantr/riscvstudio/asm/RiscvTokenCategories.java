package hk.quantr.riscvstudio.asm;

import hk.quantr.assembler.antlr.RISCVAssemblerLexer;

/**
 * Maps {@link RISCVAssemblerLexer} token types onto editor color categories.
 * Instruction vs register vs directive ranges follow {@code RISCVAssemblerParser.g4}
 * (instructions / registers / section / macro) without changing those grammars.
 */
public final class RiscvTokenCategories {

	public static final String KEYWORD = "keyword";
	public static final String REGISTER = "register";
	public static final String DIRECTIVE = "directive";
	public static final String LABEL = "label";
	public static final String IDENTIFIER = "identifier";
	public static final String NUMBER = "number";
	public static final String COMMENT = "comment";
	public static final String STRING = "string";
	public static final String OPERATOR = "operator";
	public static final String ERROR = "error";

	private RiscvTokenCategories() {
	}

	public static String category(int type) {
		if (type == RISCVAssemblerLexer.EOF || type < 1) {
			return null;
		}
		if (type == RISCVAssemblerLexer.WS || type == RISCVAssemblerLexer.NL) {
			return null;
		}
		if (type == RISCVAssemblerLexer.LINE_COMMENT) {
			return COMMENT;
		}
		if (type == RISCVAssemblerLexer.MATH_EXPRESSION) {
			return NUMBER;
		}
		if (type == RISCVAssemblerLexer.IDENTIFIER || type == RISCVAssemblerLexer.FILENAME) {
			return IDENTIFIER;
		}
		if (type == RISCVAssemblerLexer.DOUBLE_QUOTATION) {
			return STRING;
		}
		if (isDirective(type)) {
			return DIRECTIVE;
		}
		if (isOperator(type)) {
			return OPERATOR;
		}
		if (type == RISCVAssemblerLexer.INSTRUCTION
				|| type == RISCVAssemblerLexer.REGISTERS
				|| type == RISCVAssemblerLexer.MACRO
				|| type == RISCVAssemblerLexer.LINENUMBER
				|| type == RISCVAssemblerLexer.ADDRESS
				|| type == RISCVAssemblerLexer.BYTE) {
			return ERROR;
		}
		if (type >= RISCVAssemblerLexer.SLL && type < RISCVAssemblerLexer.FILENAME) {
			return KEYWORD;
		}
		if (type >= RISCVAssemblerLexer.X0 && type < RISCVAssemblerLexer.SLL) {
			return REGISTER;
		}
		if (type >= RISCVAssemblerLexer.RNE && type < RISCVAssemblerLexer.X0) {
			return KEYWORD;
		}
		return IDENTIFIER;
	}

	static boolean isDirective(int type) {
		return type == RISCVAssemblerLexer.DEFINE
				|| type == RISCVAssemblerLexer.IFDEF
				|| type == RISCVAssemblerLexer.ELIF
				|| type == RISCVAssemblerLexer.ELSE
				|| type == RISCVAssemblerLexer.ENDIF
				|| type == RISCVAssemblerLexer.INCLUDE
				|| type == RISCVAssemblerLexer.DOTBYTE
				|| type == RISCVAssemblerLexer.DOTHALF
				|| type == RISCVAssemblerLexer.DOTWORD
				|| type == RISCVAssemblerLexer.DOTDWORD
				|| type == RISCVAssemblerLexer.DOTSTRING
				|| type == RISCVAssemblerLexer.TIMES
				|| type == RISCVAssemblerLexer.DOT
				|| type == RISCVAssemblerLexer.DB_SYMBOL;
	}

	static boolean isOperator(int type) {
		return type == RISCVAssemblerLexer.ADD_
				|| type == RISCVAssemblerLexer.MIN_
				|| type == RISCVAssemblerLexer.MUL_
				|| type == RISCVAssemblerLexer.DIV_
				|| type == RISCVAssemblerLexer.MOD_
				|| type == RISCVAssemblerLexer.SQU_
				|| type == RISCVAssemblerLexer.DOLLAR
				|| type == RISCVAssemblerLexer.ARITHMETIC_SYMBOL
				|| type == RISCVAssemblerLexer.COMMA
				|| type == RISCVAssemblerLexer.COLON
				|| type == RISCVAssemblerLexer.OPEN_BIG_BRACKET
				|| type == RISCVAssemblerLexer.CLOSE_BIG_BRACKET
				|| type == RISCVAssemblerLexer.OPEN_SMALL_BRACKET
				|| type == RISCVAssemblerLexer.CLOSE_SMALL_BRACKET
				|| type == RISCVAssemblerLexer.OPEN_MID_BRACKET
				|| type == RISCVAssemblerLexer.CLOSE_MID_BRACKET;
	}
}
