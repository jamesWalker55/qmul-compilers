// Generated from f:\Users\Hero\Desktop\School\University\Y3\Compilers\qmul-compilers\src\CoolLexer.g4 by ANTLR 4.9.2
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class CoolLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.9.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		PERIOD=1, COMMA=2, AT=3, SEMICOLON=4, COLON=5, CURLY_OPEN=6, CURLY_CLOSE=7, 
		PARENT_OPEN=8, PARENT_CLOSE=9, PLUS_OPERATOR=10, MINUS_OPERATOR=11, MULT_OPERATOR=12, 
		DIV_OPERATOR=13, INT_COMPLEMENT_OPERATOR=14, LESS_OPERATOR=15, LESS_EQ_OPERATOR=16, 
		EQ_OPERATOR=17, ASSIGN_OPERATOR=18, RIGHTARROW=19, ERROR=20, CLASS=21, 
		INHERITS=22, BOOL=23, LET=24, IN=25, CASE=26, OF=27;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"PERIOD", "COMMA", "AT", "SEMICOLON", "COLON", "CURLY_OPEN", "CURLY_CLOSE", 
			"PARENT_OPEN", "PARENT_CLOSE", "PLUS_OPERATOR", "MINUS_OPERATOR", "MULT_OPERATOR", 
			"DIV_OPERATOR", "INT_COMPLEMENT_OPERATOR", "LESS_OPERATOR", "LESS_EQ_OPERATOR", 
			"EQ_OPERATOR", "ASSIGN_OPERATOR", "RIGHTARROW", "ERROR", "CLASS", "INHERITS", 
			"BOOL", "TRUE", "FALSE", "LET", "IN", "CASE", "OF"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'.'", "','", "'@'", "';'", "':'", "'{'", "'}'", "'('", "')'", 
			"'+'", "'-'", "'*'", "'/'", "'~'", "'<'", "'<='", "'='", "'<-'", "'=>'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "PERIOD", "COMMA", "AT", "SEMICOLON", "COLON", "CURLY_OPEN", "CURLY_CLOSE", 
			"PARENT_OPEN", "PARENT_CLOSE", "PLUS_OPERATOR", "MINUS_OPERATOR", "MULT_OPERATOR", 
			"DIV_OPERATOR", "INT_COMPLEMENT_OPERATOR", "LESS_OPERATOR", "LESS_EQ_OPERATOR", 
			"EQ_OPERATOR", "ASSIGN_OPERATOR", "RIGHTARROW", "ERROR", "CLASS", "INHERITS", 
			"BOOL", "LET", "IN", "CASE", "OF"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}


	public CoolLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "CoolLexer.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\35\u0094\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31"+
		"\t\31\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\3\2\3\2\3\3\3"+
		"\3\3\4\3\4\3\5\3\5\3\6\3\6\3\7\3\7\3\b\3\b\3\t\3\t\3\n\3\n\3\13\3\13\3"+
		"\f\3\f\3\r\3\r\3\16\3\16\3\17\3\17\3\20\3\20\3\21\3\21\3\21\3\22\3\22"+
		"\3\23\3\23\3\23\3\24\3\24\3\24\3\25\3\25\3\26\3\26\3\26\3\26\3\26\3\26"+
		"\3\27\3\27\3\27\3\27\3\27\3\27\3\27\3\27\3\30\3\30\5\30y\n\30\3\31\3\31"+
		"\3\31\3\31\3\31\3\32\3\32\3\32\3\32\3\32\3\32\3\33\3\33\3\33\3\33\3\34"+
		"\3\34\3\34\3\35\3\35\3\35\3\35\3\35\3\36\3\36\3\36\2\2\37\3\3\5\4\7\5"+
		"\t\6\13\7\r\b\17\t\21\n\23\13\25\f\27\r\31\16\33\17\35\20\37\21!\22#\23"+
		"%\24\'\25)\26+\27-\30/\31\61\2\63\2\65\32\67\339\34;\35\3\2\21\4\2EEe"+
		"e\4\2NNnn\4\2CCcc\4\2UUuu\4\2KKkk\4\2PPpp\4\2JJjj\4\2GGgg\4\2TTtt\4\2"+
		"VVvv\3\2vv\4\2WWww\3\2hh\4\2QQqq\4\2HHhh\2\u0092\2\3\3\2\2\2\2\5\3\2\2"+
		"\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21"+
		"\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2"+
		"\2\2\2\35\3\2\2\2\2\37\3\2\2\2\2!\3\2\2\2\2#\3\2\2\2\2%\3\2\2\2\2\'\3"+
		"\2\2\2\2)\3\2\2\2\2+\3\2\2\2\2-\3\2\2\2\2/\3\2\2\2\2\65\3\2\2\2\2\67\3"+
		"\2\2\2\29\3\2\2\2\2;\3\2\2\2\3=\3\2\2\2\5?\3\2\2\2\7A\3\2\2\2\tC\3\2\2"+
		"\2\13E\3\2\2\2\rG\3\2\2\2\17I\3\2\2\2\21K\3\2\2\2\23M\3\2\2\2\25O\3\2"+
		"\2\2\27Q\3\2\2\2\31S\3\2\2\2\33U\3\2\2\2\35W\3\2\2\2\37Y\3\2\2\2![\3\2"+
		"\2\2#^\3\2\2\2%`\3\2\2\2\'c\3\2\2\2)f\3\2\2\2+h\3\2\2\2-n\3\2\2\2/x\3"+
		"\2\2\2\61z\3\2\2\2\63\177\3\2\2\2\65\u0085\3\2\2\2\67\u0089\3\2\2\29\u008c"+
		"\3\2\2\2;\u0091\3\2\2\2=>\7\60\2\2>\4\3\2\2\2?@\7.\2\2@\6\3\2\2\2AB\7"+
		"B\2\2B\b\3\2\2\2CD\7=\2\2D\n\3\2\2\2EF\7<\2\2F\f\3\2\2\2GH\7}\2\2H\16"+
		"\3\2\2\2IJ\7\177\2\2J\20\3\2\2\2KL\7*\2\2L\22\3\2\2\2MN\7+\2\2N\24\3\2"+
		"\2\2OP\7-\2\2P\26\3\2\2\2QR\7/\2\2R\30\3\2\2\2ST\7,\2\2T\32\3\2\2\2UV"+
		"\7\61\2\2V\34\3\2\2\2WX\7\u0080\2\2X\36\3\2\2\2YZ\7>\2\2Z \3\2\2\2[\\"+
		"\7>\2\2\\]\7?\2\2]\"\3\2\2\2^_\7?\2\2_$\3\2\2\2`a\7>\2\2ab\7/\2\2b&\3"+
		"\2\2\2cd\7?\2\2de\7@\2\2e(\3\2\2\2fg\13\2\2\2g*\3\2\2\2hi\t\2\2\2ij\t"+
		"\3\2\2jk\t\4\2\2kl\t\5\2\2lm\t\5\2\2m,\3\2\2\2no\t\6\2\2op\t\7\2\2pq\t"+
		"\b\2\2qr\t\t\2\2rs\t\n\2\2st\t\6\2\2tu\t\13\2\2u.\3\2\2\2vy\5\61\31\2"+
		"wy\5\63\32\2xv\3\2\2\2xw\3\2\2\2y\60\3\2\2\2z{\t\f\2\2{|\t\n\2\2|}\t\r"+
		"\2\2}~\t\t\2\2~\62\3\2\2\2\177\u0080\t\16\2\2\u0080\u0081\t\4\2\2\u0081"+
		"\u0082\t\3\2\2\u0082\u0083\t\5\2\2\u0083\u0084\t\t\2\2\u0084\64\3\2\2"+
		"\2\u0085\u0086\t\3\2\2\u0086\u0087\t\t\2\2\u0087\u0088\t\13\2\2\u0088"+
		"\66\3\2\2\2\u0089\u008a\t\6\2\2\u008a\u008b\t\7\2\2\u008b8\3\2\2\2\u008c"+
		"\u008d\t\2\2\2\u008d\u008e\t\4\2\2\u008e\u008f\t\5\2\2\u008f\u0090\t\t"+
		"\2\2\u0090:\3\2\2\2\u0091\u0092\t\17\2\2\u0092\u0093\t\20\2\2\u0093<\3"+
		"\2\2\2\4\2x\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}