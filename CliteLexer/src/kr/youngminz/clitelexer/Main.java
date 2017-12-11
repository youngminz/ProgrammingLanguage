package kr.youngminz.clitelexer;

public class Main {
    public static void main(String args[]) {
        Parser parser = new Parser(new Lexer(args[0]));
        Program prog = parser.program();
        System.out.println("---------------------------------------------------");
        prog.display(); // display abstract syntax tree

        StaticTypeCheck st=new StaticTypeCheck();
        System.out.println("Beginning type checking...");
        System.out.print("Type map:");
        TypeMap map = st.typing(prog.decpart);
        // map.display();   // student exercise
        st.V(prog);
        System.out.println(map);
    }

}
