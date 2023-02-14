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
		EQ_OPERATOR=17, ASSIGN_OPERATOR=18, RIGHTARROW=19, CLASS=20, END_CLASS=21, 
		INHERITS=22, LET=23, IN=24, CASE=25, OF=26, ESAC=27, IF=28, THEN=29, ELSE=30, 
		FI=31, WHILE=32, LOOP=33, NEW=34, ISVOID=35, NOT=36, BOOLEAN=37, INTEGER=38, 
		END_STRING=39, TYPE=40, COMMENT=41, ID=42, WS=43, ERROR=44, BEGIN_STRING=45;
	public static final int
		CLASS_MODE=1, STRING_MODE=2;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE", "CLASS_MODE", "STRING_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"PERIOD", "COMMA", "AT", "SEMICOLON", "COLON", "CURLY_OPEN", "CURLY_CLOSE", 
			"PARENT_OPEN", "PARENT_CLOSE", "PLUS_OPERATOR", "MINUS_OPERATOR", "MULT_OPERATOR", 
			"DIV_OPERATOR", "INT_COMPLEMENT_OPERATOR", "LESS_OPERATOR", "LESS_EQ_OPERATOR", 
			"EQ_OPERATOR", "ASSIGN_OPERATOR", "RIGHTARROW", "CLASS", "END_CLASS", 
			"NAME", "INHERITS", "LET", "IN", "CASE", "OF", "ESAC", "IF", "THEN", 
			"ELSE", "FI", "WHILE", "LOOP", "NEW", "ISVOID", "NOT", "BOOLEAN", "TRUE", 
			"FALSE", "INTEGER", "DIGIT", "BEGIN_STRING", "END_STRING", "TEXT", "TYPE", 
			"IO", "SELF_TYPE", "OBJECT", "INT", "STRING", "BOOL", "COMMENT", "ID", 
			"LETTER", "WS", "ERROR"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'.'", "','", "'@'", "';'", "':'", "'{'", "'}'", "'('", "')'", 
			"'+'", "'-'", "'*'", "'/'", "'~'", "'<'", "'<='", "'='", "'<-'", "'=>'", 
			null, "' '"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "PERIOD", "COMMA", "AT", "SEMICOLON", "COLON", "CURLY_OPEN", "CURLY_CLOSE", 
			"PARENT_OPEN", "PARENT_CLOSE", "PLUS_OPERATOR", "MINUS_OPERATOR", "MULT_OPERATOR", 
			"DIV_OPERATOR", "INT_COMPLEMENT_OPERATOR", "LESS_OPERATOR", "LESS_EQ_OPERATOR", 
			"EQ_OPERATOR", "ASSIGN_OPERATOR", "RIGHTARROW", "CLASS", "END_CLASS", 
			"INHERITS", "LET", "IN", "CASE", "OF", "ESAC", "IF", "THEN", "ELSE", 
			"FI", "WHILE", "LOOP", "NEW", "ISVOID", "NOT", "BOOLEAN", "INTEGER", 
			"END_STRING", "TYPE", "COMMENT", "ID", "WS", "ERROR", "BEGIN_STRING"
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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2/\u0166\b\1\b\1\b"+
		"\1\4\2\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n"+
		"\t\n\4\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21"+
		"\4\22\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30"+
		"\4\31\t\31\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37"+
		"\4 \t \4!\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t"+
		"*\4+\t+\4,\t,\4-\t-\4.\t.\4/\t/\4\60\t\60\4\61\t\61\4\62\t\62\4\63\t\63"+
		"\4\64\t\64\4\65\t\65\4\66\t\66\4\67\t\67\48\t8\49\t9\4:\t:\3\2\3\2\3\3"+
		"\3\3\3\4\3\4\3\5\3\5\3\6\3\6\3\7\3\7\3\b\3\b\3\t\3\t\3\n\3\n\3\13\3\13"+
		"\3\f\3\f\3\r\3\r\3\16\3\16\3\17\3\17\3\20\3\20\3\21\3\21\3\21\3\22\3\22"+
		"\3\23\3\23\3\23\3\24\3\24\3\24\3\25\3\25\3\25\3\25\3\25\3\25\6\25\u00a7"+
		"\n\25\r\25\16\25\u00a8\3\25\3\25\3\26\3\26\3\26\3\26\3\27\3\27\3\27\3"+
		"\27\3\30\3\30\3\30\3\30\3\30\3\30\3\30\3\30\3\30\3\31\3\31\3\31\3\31\3"+
		"\32\3\32\3\32\3\33\3\33\3\33\3\33\3\33\3\34\3\34\3\34\3\35\3\35\3\35\3"+
		"\35\3\35\3\36\3\36\3\36\3\37\3\37\3\37\3\37\3\37\3 \3 \3 \3 \3 \3!\3!"+
		"\3!\3\"\3\"\3\"\3\"\3\"\3\"\3#\3#\3#\3#\3#\3$\3$\3$\3$\3%\3%\3%\3%\3%"+
		"\3%\3%\3&\3&\3&\3&\3\'\3\'\5\'\u00fe\n\'\3(\3(\3(\3(\3(\3)\3)\3)\3)\3"+
		")\3)\3*\6*\u010c\n*\r*\16*\u010d\3+\3+\3,\3,\3,\3,\3,\3-\3-\3-\3-\3.\3"+
		".\3.\3.\3/\3/\3/\3/\3/\3/\5/\u0125\n/\3\60\3\60\3\60\3\61\3\61\3\61\3"+
		"\61\3\61\3\61\3\61\3\61\3\61\3\61\3\62\3\62\3\62\3\62\3\62\3\62\3\62\3"+
		"\63\3\63\3\63\3\63\3\64\3\64\3\64\3\64\3\64\3\64\3\64\3\65\3\65\3\65\3"+
		"\65\3\65\3\66\3\66\3\66\3\66\3\66\7\66\u0150\n\66\f\66\16\66\u0153\13"+
		"\66\3\66\3\66\3\66\3\66\3\66\3\67\3\67\38\38\39\69\u015f\n9\r9\169\u0160"+
		"\39\39\3:\3:\3\u0151\2;\5\3\7\4\t\5\13\6\r\7\17\b\21\t\23\n\25\13\27\f"+
		"\31\r\33\16\35\17\37\20!\21#\22%\23\'\24)\25+\26-\27/\2\61\30\63\31\65"+
		"\32\67\339\34;\35=\36?\37A C!E\"G#I$K%M&O\'Q\2S\2U(W\2Y/[)]\2_*a\2c\2"+
		"e\2g\2i\2k\2m+o,q\2s-u.\5\2\3\4-\4\2EEee\4\2NNnn\4\2CCcc\4\2UUuu\4\2K"+
		"Kkk\4\2PPpp\4\2JJjj\4\2GGgg\4\2TTtt\4\2VVvv\4\2QQqq\4\2HHhh\4\2YYyy\4"+
		"\2RRrr\4\2XXxx\4\2FFff\3\2vv\4\2WWww\3\2hh\3\2\62;\3\2KK\3\2QQ\3\2UU\3"+
		"\2GG\3\2NN\3\2HH\3\2aa\3\2VV\3\2[[\3\2RR\3\2dd\3\2ll\3\2gg\3\2ee\3\2p"+
		"p\3\2tt\3\2kk\3\2ii\3\2DD\3\2qq\3\2c|\4\2C\\c|\5\2\13\f\17\17\"\"\2\u0164"+
		"\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2"+
		"\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2"+
		"\2\33\3\2\2\2\2\35\3\2\2\2\2\37\3\2\2\2\2!\3\2\2\2\2#\3\2\2\2\2%\3\2\2"+
		"\2\2\'\3\2\2\2\2)\3\2\2\2\2+\3\2\2\2\2\61\3\2\2\2\2\63\3\2\2\2\2\65\3"+
		"\2\2\2\2\67\3\2\2\2\29\3\2\2\2\2;\3\2\2\2\2=\3\2\2\2\2?\3\2\2\2\2A\3\2"+
		"\2\2\2C\3\2\2\2\2E\3\2\2\2\2G\3\2\2\2\2I\3\2\2\2\2K\3\2\2\2\2M\3\2\2\2"+
		"\2O\3\2\2\2\2U\3\2\2\2\2Y\3\2\2\2\2_\3\2\2\2\2m\3\2\2\2\2o\3\2\2\2\2s"+
		"\3\2\2\2\2u\3\2\2\2\3-\3\2\2\2\3/\3\2\2\2\4[\3\2\2\2\4]\3\2\2\2\5w\3\2"+
		"\2\2\7y\3\2\2\2\t{\3\2\2\2\13}\3\2\2\2\r\177\3\2\2\2\17\u0081\3\2\2\2"+
		"\21\u0083\3\2\2\2\23\u0085\3\2\2\2\25\u0087\3\2\2\2\27\u0089\3\2\2\2\31"+
		"\u008b\3\2\2\2\33\u008d\3\2\2\2\35\u008f\3\2\2\2\37\u0091\3\2\2\2!\u0093"+
		"\3\2\2\2#\u0095\3\2\2\2%\u0098\3\2\2\2\'\u009a\3\2\2\2)\u009d\3\2\2\2"+
		"+\u00a0\3\2\2\2-\u00ac\3\2\2\2/\u00b0\3\2\2\2\61\u00b4\3\2\2\2\63\u00bd"+
		"\3\2\2\2\65\u00c1\3\2\2\2\67\u00c4\3\2\2\29\u00c9\3\2\2\2;\u00cc\3\2\2"+
		"\2=\u00d1\3\2\2\2?\u00d4\3\2\2\2A\u00d9\3\2\2\2C\u00de\3\2\2\2E\u00e1"+
		"\3\2\2\2G\u00e7\3\2\2\2I\u00ec\3\2\2\2K\u00f0\3\2\2\2M\u00f7\3\2\2\2O"+
		"\u00fd\3\2\2\2Q\u00ff\3\2\2\2S\u0104\3\2\2\2U\u010b\3\2\2\2W\u010f\3\2"+
		"\2\2Y\u0111\3\2\2\2[\u0116\3\2\2\2]\u011a\3\2\2\2_\u0124\3\2\2\2a\u0126"+
		"\3\2\2\2c\u0129\3\2\2\2e\u0133\3\2\2\2g\u013a\3\2\2\2i\u013e\3\2\2\2k"+
		"\u0145\3\2\2\2m\u014a\3\2\2\2o\u0159\3\2\2\2q\u015b\3\2\2\2s\u015e\3\2"+
		"\2\2u\u0164\3\2\2\2wx\7\60\2\2x\6\3\2\2\2yz\7.\2\2z\b\3\2\2\2{|\7B\2\2"+
		"|\n\3\2\2\2}~\7=\2\2~\f\3\2\2\2\177\u0080\7<\2\2\u0080\16\3\2\2\2\u0081"+
		"\u0082\7}\2\2\u0082\20\3\2\2\2\u0083\u0084\7\177\2\2\u0084\22\3\2\2\2"+
		"\u0085\u0086\7*\2\2\u0086\24\3\2\2\2\u0087\u0088\7+\2\2\u0088\26\3\2\2"+
		"\2\u0089\u008a\7-\2\2\u008a\30\3\2\2\2\u008b\u008c\7/\2\2\u008c\32\3\2"+
		"\2\2\u008d\u008e\7,\2\2\u008e\34\3\2\2\2\u008f\u0090\7\61\2\2\u0090\36"+
		"\3\2\2\2\u0091\u0092\7\u0080\2\2\u0092 \3\2\2\2\u0093\u0094\7>\2\2\u0094"+
		"\"\3\2\2\2\u0095\u0096\7>\2\2\u0096\u0097\7?\2\2\u0097$\3\2\2\2\u0098"+
		"\u0099\7?\2\2\u0099&\3\2\2\2\u009a\u009b\7>\2\2\u009b\u009c\7/\2\2\u009c"+
		"(\3\2\2\2\u009d\u009e\7?\2\2\u009e\u009f\7@\2\2\u009f*\3\2\2\2\u00a0\u00a1"+
		"\t\2\2\2\u00a1\u00a2\t\3\2\2\u00a2\u00a3\t\4\2\2\u00a3\u00a4\t\5\2\2\u00a4"+
		"\u00a6\t\5\2\2\u00a5\u00a7\7\"\2\2\u00a6\u00a5\3\2\2\2\u00a7\u00a8\3\2"+
		"\2\2\u00a8\u00a6\3\2\2\2\u00a8\u00a9\3\2\2\2\u00a9\u00aa\3\2\2\2\u00aa"+
		"\u00ab\b\25\2\2\u00ab,\3\2\2\2\u00ac\u00ad\7\"\2\2\u00ad\u00ae\3\2\2\2"+
		"\u00ae\u00af\b\26\3\2\u00af.\3\2\2\2\u00b0\u00b1\13\2\2\2\u00b1\u00b2"+
		"\3\2\2\2\u00b2\u00b3\b\27\4\2\u00b3\60\3\2\2\2\u00b4\u00b5\t\6\2\2\u00b5"+
		"\u00b6\t\7\2\2\u00b6\u00b7\t\b\2\2\u00b7\u00b8\t\t\2\2\u00b8\u00b9\t\n"+
		"\2\2\u00b9\u00ba\t\6\2\2\u00ba\u00bb\t\13\2\2\u00bb\u00bc\t\5\2\2\u00bc"+
		"\62\3\2\2\2\u00bd\u00be\t\3\2\2\u00be\u00bf\t\t\2\2\u00bf\u00c0\t\13\2"+
		"\2\u00c0\64\3\2\2\2\u00c1\u00c2\t\6\2\2\u00c2\u00c3\t\7\2\2\u00c3\66\3"+
		"\2\2\2\u00c4\u00c5\t\2\2\2\u00c5\u00c6\t\4\2\2\u00c6\u00c7\t\5\2\2\u00c7"+
		"\u00c8\t\t\2\2\u00c88\3\2\2\2\u00c9\u00ca\t\f\2\2\u00ca\u00cb\t\r\2\2"+
		"\u00cb:\3\2\2\2\u00cc\u00cd\t\t\2\2\u00cd\u00ce\t\5\2\2\u00ce\u00cf\t"+
		"\4\2\2\u00cf\u00d0\t\2\2\2\u00d0<\3\2\2\2\u00d1\u00d2\t\6\2\2\u00d2\u00d3"+
		"\t\r\2\2\u00d3>\3\2\2\2\u00d4\u00d5\t\13\2\2\u00d5\u00d6\t\b\2\2\u00d6"+
		"\u00d7\t\t\2\2\u00d7\u00d8\t\7\2\2\u00d8@\3\2\2\2\u00d9\u00da\t\t\2\2"+
		"\u00da\u00db\t\3\2\2\u00db\u00dc\t\5\2\2\u00dc\u00dd\t\t\2\2\u00ddB\3"+
		"\2\2\2\u00de\u00df\t\r\2\2\u00df\u00e0\t\6\2\2\u00e0D\3\2\2\2\u00e1\u00e2"+
		"\t\16\2\2\u00e2\u00e3\t\b\2\2\u00e3\u00e4\t\6\2\2\u00e4\u00e5\t\3\2\2"+
		"\u00e5\u00e6\t\t\2\2\u00e6F\3\2\2\2\u00e7\u00e8\t\3\2\2\u00e8\u00e9\t"+
		"\f\2\2\u00e9\u00ea\t\f\2\2\u00ea\u00eb\t\17\2\2\u00ebH\3\2\2\2\u00ec\u00ed"+
		"\t\7\2\2\u00ed\u00ee\t\t\2\2\u00ee\u00ef\t\16\2\2\u00efJ\3\2\2\2\u00f0"+
		"\u00f1\t\6\2\2\u00f1\u00f2\t\5\2\2\u00f2\u00f3\t\20\2\2\u00f3\u00f4\t"+
		"\f\2\2\u00f4\u00f5\t\6\2\2\u00f5\u00f6\t\21\2\2\u00f6L\3\2\2\2\u00f7\u00f8"+
		"\t\7\2\2\u00f8\u00f9\t\f\2\2\u00f9\u00fa\t\13\2\2\u00faN\3\2\2\2\u00fb"+
		"\u00fe\5Q(\2\u00fc\u00fe\5S)\2\u00fd\u00fb\3\2\2\2\u00fd\u00fc\3\2\2\2"+
		"\u00feP\3\2\2\2\u00ff\u0100\t\22\2\2\u0100\u0101\t\n\2\2\u0101\u0102\t"+
		"\23\2\2\u0102\u0103\t\t\2\2\u0103R\3\2\2\2\u0104\u0105\t\24\2\2\u0105"+
		"\u0106\t\4\2\2\u0106\u0107\t\3\2\2\u0107\u0108\t\5\2\2\u0108\u0109\t\t"+
		"\2\2\u0109T\3\2\2\2\u010a\u010c\5W+\2\u010b\u010a\3\2\2\2\u010c\u010d"+
		"\3\2\2\2\u010d\u010b\3\2\2\2\u010d\u010e\3\2\2\2\u010eV\3\2\2\2\u010f"+
		"\u0110\t\25\2\2\u0110X\3\2\2\2\u0111\u0112\7$\2\2\u0112\u0113\3\2\2\2"+
		"\u0113\u0114\b,\4\2\u0114\u0115\b,\5\2\u0115Z\3\2\2\2\u0116\u0117\7$\2"+
		"\2\u0117\u0118\3\2\2\2\u0118\u0119\b-\3\2\u0119\\\3\2\2\2\u011a\u011b"+
		"\13\2\2\2\u011b\u011c\3\2\2\2\u011c\u011d\b.\4\2\u011d^\3\2\2\2\u011e"+
		"\u0125\5e\62\2\u011f\u0125\5g\63\2\u0120\u0125\5i\64\2\u0121\u0125\5k"+
		"\65\2\u0122\u0125\5a\60\2\u0123\u0125\5c\61\2\u0124\u011e\3\2\2\2\u0124"+
		"\u011f\3\2\2\2\u0124\u0120\3\2\2\2\u0124\u0121\3\2\2\2\u0124\u0122\3\2"+
		"\2\2\u0124\u0123\3\2\2\2\u0125`\3\2\2\2\u0126\u0127\t\26\2\2\u0127\u0128"+
		"\t\27\2\2\u0128b\3\2\2\2\u0129\u012a\t\30\2\2\u012a\u012b\t\31\2\2\u012b"+
		"\u012c\t\32\2\2\u012c\u012d\t\33\2\2\u012d\u012e\t\34\2\2\u012e\u012f"+
		"\t\35\2\2\u012f\u0130\t\36\2\2\u0130\u0131\t\37\2\2\u0131\u0132\t\31\2"+
		"\2\u0132d\3\2\2\2\u0133\u0134\t\27\2\2\u0134\u0135\t \2\2\u0135\u0136"+
		"\t!\2\2\u0136\u0137\t\"\2\2\u0137\u0138\t#\2\2\u0138\u0139\t\22\2\2\u0139"+
		"f\3\2\2\2\u013a\u013b\t\26\2\2\u013b\u013c\t$\2\2\u013c\u013d\t\22\2\2"+
		"\u013dh\3\2\2\2\u013e\u013f\t\30\2\2\u013f\u0140\t\22\2\2\u0140\u0141"+
		"\t%\2\2\u0141\u0142\t&\2\2\u0142\u0143\t$\2\2\u0143\u0144\t\'\2\2\u0144"+
		"j\3\2\2\2\u0145\u0146\t(\2\2\u0146\u0147\t)\2\2\u0147\u0148\t)\2\2\u0148"+
		"\u0149\t\32\2\2\u0149l\3\2\2\2\u014a\u014b\7*\2\2\u014b\u014c\7,\2\2\u014c"+
		"\u0151\3\2\2\2\u014d\u0150\5m\66\2\u014e\u0150\13\2\2\2\u014f\u014d\3"+
		"\2\2\2\u014f\u014e\3\2\2\2\u0150\u0153\3\2\2\2\u0151\u0152\3\2\2\2\u0151"+
		"\u014f\3\2\2\2\u0152\u0154\3\2\2\2\u0153\u0151\3\2\2\2\u0154\u0155\7,"+
		"\2\2\u0155\u0156\7+\2\2\u0156\u0157\3\2\2\2\u0157\u0158\b\66\6\2\u0158"+
		"n\3\2\2\2\u0159\u015a\t*\2\2\u015ap\3\2\2\2\u015b\u015c\t+\2\2\u015cr"+
		"\3\2\2\2\u015d\u015f\t,\2\2\u015e\u015d\3\2\2\2\u015f\u0160\3\2\2\2\u0160"+
		"\u015e\3\2\2\2\u0160\u0161\3\2\2\2\u0161\u0162\3\2\2\2\u0162\u0163\b9"+
		"\6\2\u0163t\3\2\2\2\u0164\u0165\13\2\2\2\u0165v\3\2\2\2\f\2\3\4\u00a8"+
		"\u00fd\u010d\u0124\u014f\u0151\u0160\7\4\3\2\4\2\2\5\2\2\4\4\2\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}