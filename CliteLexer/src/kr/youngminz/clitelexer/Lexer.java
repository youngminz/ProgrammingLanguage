package kr.youngminz.clitelexer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Lexer {

    private final String letters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
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
        Token result;
        do {
            if (isLetter(ch)) { // ident or keyword
                String spelling = concat(letters + digits);
                result = Token.keyword(spelling);
                System.out.println(result);
                return result;
            } else if (isDigit(ch)) { // int or float literal
                String number = concat(digits);
                if (ch != '.') {  // int Literal
                    result = Token.mkIntLiteral(number);
                    System.out.println(result);
                    return result;
                }
                number += concat(digits);
                result = Token.mkFloatLiteral(number);
                System.out.println(result);
                return result;
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
                    if (ch != '/') {
                        result = Token.divideTok;
                        System.out.println(result);
                        return result;
                    }
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
                    result = Token.mkCharLiteral("" + ch1);
                    System.out.println(result);
                    return result;

                case eofCh:
                    result = Token.eofTok;
                    System.out.println(result);
                    return result;

                case '+':
                    ch = nextChar();
                    result = Token.plusTok;
                    System.out.println(result);
                    return result;

                // NOTE: 이렇게 그냥 체크 안 하고 읽기만 해도 되나.....?
                // - * ( ) { } ; ,  student exercise
                case '-':
                    ch = nextChar();
                    result = Token.minusTok;
                    System.out.println(result);
                    return result;

                case '*':
                    ch = nextChar();
                    result = Token.multiplyTok;
                    System.out.println(result);
                    return result;

                case '(':
                    ch = nextChar();
                    result = Token.leftParenTok;
                    System.out.println(result);
                    return result;

                case ')':
                    ch = nextChar();
                    result = Token.rightParenTok;
                    System.out.println(result);
                    return result;

                case '{':
                    ch = nextChar();
                    result = Token.leftBraceTok;
                    System.out.println(result);
                    return result;

                case '}':
                    ch = nextChar();
                    result = Token.rightBraceTok;
                    System.out.println(result);
                    return result;

                case '[':
                    ch = nextChar();
                    result = Token.leftBracketTok;
                    System.out.println(result);
                    return result;

                case ']':
                    ch = nextChar();
                    result = Token.rightBracketTok;
                    System.out.println(result);
                    return result;

                case ';':
                    ch = nextChar();
                    result = Token.semicolonTok;
                    System.out.println(result);
                    return result;

                case ',':
                    ch = nextChar();
                    result = Token.commaTok;
                    System.out.println(result);
                    return result;

                case '&':
                    check('&');
                    result = Token.andTok;
                    System.out.println(result);
                    return result;
                case '|':
                    check('|');
                    result = Token.orTok;
                    System.out.println(result);
                    return result;

                // NOTE: 첫 번째 글자는 읽었다. 두 번째 문자가 무엇이냐에 따라 잘 처신하면 된다.
                case '=':
                    ch = nextChar();
                    result = chkOpt(ch, Token.assignTok, Token.eqeqTok);
                    System.out.println(result);
                    return result;

                case '<':
                    ch = nextChar();
                    result = chkOpt(ch, Token.ltTok, Token.lteqTok);
                    System.out.println(result);
                    return result;

                case '>':
                    ch = nextChar();
                    result = chkOpt(ch, Token.gtTok, Token.gteqTok);
                    System.out.println(result);
                    return result;

                case '!':
                    ch = nextChar();
                    result = chkOpt(ch, Token.notTok, Token.noteqTok);
                    System.out.println(result);
                    return result;

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
        if (one.value().length() > 1 && one.value().charAt(1) == c) {
            ch = nextChar();
            return one;
        } else if (two.value().length() > 1 && two.value().charAt(1) == c) {
            ch = nextChar();
            return two;
        } else if (one.value().length() == 1) {
            return one;
        } else if (two.value().length() == 1) {
            return two;
        } else {
            error("invalid character");
            return null;
        }
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
        for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
            System.out.println(ste);
        }
        System.exit(1);
    }

}

