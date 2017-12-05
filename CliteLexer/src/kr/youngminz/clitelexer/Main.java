package kr.youngminz.clitelexer;

public class Main {
    public static void main(String args[]) {
        Parser parser = new Parser(new Lexer(args[0]));
        Program prog = parser.program();
        prog.display(); // display abstract syntax tree
    }
}
