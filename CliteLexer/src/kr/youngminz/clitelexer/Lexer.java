package kr.youngminz.clitelexer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Lexer {

    private final String letters = "abcdefghijklmnopqrstuvwxyz"
            + "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private final String digits = "0123456789";
    private final char eolnCh = '\n';
    private final char eofCh = '\004';
    private boolean isEof = false;
    private char ch = ' ';
    private BufferedReader input;
    private String line = "";
    private int lineno = 0;
    private int col = 1;


    public Lexer(String fileName) { // source filename
        System.out.println(System.getProperty("user.dir"));
        try {
            input = new BufferedReader(new FileReader(fileName));
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + fileName);
            System.exit(1);
        }
    }

    static public void main(String[] argv) {
        Lexer lexer = new Lexer(argv[0]);
        Token tok = lexer.next();
        while (tok != Token.eofTok) {
            System.out.println(tok.toString());
            tok = lexer.next();
        }
    } // main

    private char nextChar() { // Return next char
        if (ch == eofCh)
            error("Attempt to read past end of file");
        col++;
        if (col >= line.length()) {
            try {
                line = input.readLine();
            } catch (IOException e) {
                System.err.println(e);
                System.exit(1);
            } // try
            if (line == null) // at end of file
                line = "" + eofCh;
            else {
                // System.out.println(lineno + ":\t" + line);
                lineno++;
                line += eolnCh;
            } // if line
            col = 0;
        } // if col
        return line.charAt(col);
    }

    public Token next() { // Return next token
        do {
            if (isLetter(ch)) { // ident or keyword
                String spelling = concat(letters + digits);
                return Token.keyword(spelling);
            } else if (isDigit(ch)) { // int or float literal
                String number = concat(digits);
                if (ch != '.')  // int Literal
                    return Token.mkIntLiteral(number);
                number += concat(digits);
                return Token.mkFloatLiteral(number);
            } else switch (ch) {
                case ' ':
                case '\t':
                case '\r':
                case eolnCh:
                    // NOTE: 소스 코드를 읽는 중 무시할 수 있는 문자들임.
                    // 그냥 Discard 하고 다음 문자를 읽으면 됨.
                    ch = nextChar();
                    break;

                case '/':  // divide or comment
                    ch = nextChar();
                    if (ch != '/') return Token.divideTok;
                    // comment
                    // NOTE: /를 읽었는데 다음 문자도 /이면, 주석으로 처리하고 End of Line 까지 읽으면 됨.
                    do {
                        ch = nextChar();
                    } while (ch != eolnCh);
                    ch = nextChar();
                    break;

                case '\'':  // char literal
                    char ch1 = nextChar();
                    nextChar(); // get '
                    ch = nextChar();
                    return Token.mkCharLiteral("" + ch1);

                case eofCh:
                    return Token.eofTok;

                case '+':
                    ch = nextChar();
                    return Token.plusTok;

                // NOTE: 이렇게 그냥 체크 안 하고 읽기만 해도 되나.....?
                // - * ( ) { } ; ,  student exercise
                case '-':
                    ch = nextChar();
                    return Token.minusTok;

                case '*':
                    ch = nextChar();
                    return Token.multiplyTok;

                case '(':
                    ch = nextChar();
                    return Token.leftParenTok;

                case ')':
                    ch = nextChar();
                    return Token.rightParenTok;

                case '{':
                    ch = nextChar();
                    return Token.leftBraceTok;

                case '}':
                    ch = nextChar();
                    return Token.rightBraceTok;

                case ';':
                    ch = nextChar();
                    return Token.semicolonTok;

                case ',':
                    ch = nextChar();
                    return Token.commaTok;

                case '&':
                    check('&');
                    return Token.andTok;
                case '|':
                    check('|');
                    return Token.orTok;

                case '=':
                    return chkOpt('=', Token.assignTok,
                            Token.eqeqTok);

                case '<':
                    return chkOpt('<', Token.ltTok, Token.lteqTok);

                default:
                    error("Illegal character " + ch);
            } // switch
        } while (true);
    } // next

    private boolean isLetter(char c) {
        return ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'));
    }

    private boolean isDigit(char c) {
        return '0' <= c && c <= '9';
    }

    private void check(char c) {
        ch = nextChar();
        if (ch != c)
            error("Illegal character, expecting " + c);
        ch = nextChar();
    }

    private Token chkOpt(char c, Token one, Token two) {
        return null;  // TODO student exercise
    }

    private String concat(String set) {
        // NOTE: set 변수에 있는 것들은 allowed character. allowed character 들이 나오지 않을 때까지 읽어들인다.
        StringBuilder r = new StringBuilder();
        do {
            r.append(ch);
            ch = nextChar();
        } while (set.indexOf(ch) >= 0);
        return r.toString();
    }

    public void error(String msg) {
        System.err.print(line);
        System.err.println("Error: column " + col + " " + msg);
        System.exit(1);
    }

}
