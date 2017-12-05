package kr.youngminz.clitelexer;

public class Token {

    // NOTE: 이 클래스 변수들은 위에서부터 아래로 차근차근 eval 되기 때문에,
    // 이 순서를 변경하면 NPE가 발생할 수 있다.

    private static final int KEYWORDS = TokenType.Eof.ordinal();
    private static final String[] reserved = new String[KEYWORDS];
    private static Token[] token = new Token[KEYWORDS];

    private TokenType type;
    private String value = "";

    public static final Token eofTok = new Token(TokenType.Eof, "<<EOF>>");
    public static final Token boolTok = new Token(TokenType.Bool, "논리");
    public static final Token charTok = new Token(TokenType.Char, "문자");
    public static final Token elseTok = new Token(TokenType.Else, "아니면");
    public static final Token falseTok = new Token(TokenType.False, "거짓");
    public static final Token floatTok = new Token(TokenType.Float, "실수");
    public static final Token ifTok = new Token(TokenType.If, "만약");
    public static final Token intTok = new Token(TokenType.Int, "정수");
    public static final Token declTok = new Token(TokenType.Decl, "선언");
    public static final Token mainTok = new Token(TokenType.Main, "시작");
    public static final Token trueTok = new Token(TokenType.True, "참");
    public static final Token whileTok = new Token(TokenType.While, "반복");
    public static final Token leftBraceTok = new Token(TokenType.LeftBrace, "{");
    public static final Token rightBraceTok = new Token(TokenType.RightBrace, "}");
    public static final Token leftBracketTok = new Token(TokenType.LeftBracket, "[");
    public static final Token rightBracketTok = new Token(TokenType.RightBracket, "]");
    public static final Token leftParenTok = new Token(TokenType.LeftParen, "(");
    public static final Token rightParenTok = new Token(TokenType.RightParen, ")");
    public static final Token semicolonTok = new Token(TokenType.Semicolon, ";");
    public static final Token commaTok = new Token(TokenType.Comma, ",");
    public static final Token assignTok = new Token(TokenType.Assign, "=");
    public static final Token eqeqTok = new Token(TokenType.Equals, "==");
    public static final Token ltTok = new Token(TokenType.Less, "<");
    public static final Token lteqTok = new Token(TokenType.LessEqual, "<=");
    public static final Token gtTok = new Token(TokenType.Greater, ">");
    public static final Token gteqTok = new Token(TokenType.GreaterEqual, ">=");
    public static final Token notTok = new Token(TokenType.Not, "!");
    public static final Token noteqTok = new Token(TokenType.NotEqual, "!=");
    public static final Token plusTok = new Token(TokenType.Plus, "+");
    public static final Token minusTok = new Token(TokenType.Minus, "-");
    public static final Token multiplyTok = new Token(TokenType.Multiply, "*");
    public static final Token divideTok = new Token(TokenType.Divide, "/");
    public static final Token andTok = new Token(TokenType.And, "&&");
    public static final Token orTok = new Token(TokenType.Or, "||");
    public static final Token colonTok = new Token(TokenType.Colon,":");

    private Token(TokenType t, String v) {
        type = t;
        value = v;
        if (t.compareTo(TokenType.Eof) < 0) {
            int ti = t.ordinal();
            reserved[ti] = v;
            token[ti] = this;
        }
    }

    public static Token keyword(String name) {
        char ch = name.charAt(0);
        if (ch >= 'A' && ch <= 'Z') return mkIdentTok(name);
        for (int i = 0; i < KEYWORDS; i++)
            if (name.equals(reserved[i])) return token[i];
        return mkIdentTok(name);
    } // keyword

    public static Token mkIdentTok(String name) {
        return new Token(TokenType.Identifier, name);
    }

    public static Token mkIntLiteral(String name) {
        return new Token(TokenType.IntLiteral, name);
    }

    public static Token mkFloatLiteral(String name) {
        return new Token(TokenType.FloatLiteral, name);
    }

    public static Token mkCharLiteral(String name) {
        return new Token(TokenType.CharLiteral, name);
    }

    public static void main(String[] args) {
        System.out.println(eofTok);
        System.out.println(whileTok);
    }

    public TokenType type() {
        return type;
    }

    public String value() {
        return value;
    }

    public String toString() {
        if (type.compareTo(TokenType.Identifier) < 0) return value;
        return type + "\t" + value;
    } // toString
} // Token
