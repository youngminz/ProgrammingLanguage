package kr.youngminz.clitelexer;

import java.util.*;

public class StaticTypeCheck {

    public static TypeMap typing (Declarations d) {
        TypeMap map = new TypeMap();
        for (Declaration di : d)
            map.put (di.v, di.t);
        return map;
    }

    public static void check(boolean test, String msg) {
        if (test)  return;
        System.err.println(msg);
        System.exit(1);
    }

    public static void V (Declarations d) {
        for (int i=0; i<d.size() - 1; i++)
            for (int j=i+1; j<d.size(); j++) {
                Declaration di = d.get(i);
                Declaration dj = d.get(j);
                check( ! (di.v.equals(dj.v)),
                        "duplicate declaration: " + dj.v);
            }
    }

    public static void V (Program p) {
        V (p.decpart);
        V (p.body, typing (p.decpart));
    }

    public static Type typeOf (Expression e, TypeMap tm) { // tm = TypeMap which is a tuple (v, k)
        if (e instanceof Value) return ((Value)e).type;
        if (e instanceof Variable) {
            Variable v = (Variable)e; // containsKey is a hashMap method which takes an object and returns a bool

            if(v.toString().contains("[")) {
                String tmp=v.toString();
                int index=tmp.indexOf("[");
                String tmp_change=tmp.substring(0,index);
                Variable tmp2 = new Variable(tmp_change);
                v=tmp2;
            }
            check (tm.containsKey(v), "undefined variable: " + v);
            return (Type) tm.get(v);
        }
        if (e instanceof Binary) {
            Binary b = (Binary)e;
            if (b.op.ArithmeticOp( ))
                if (typeOf(b.term1,tm)== Type.FLOAT)
                    return (Type.FLOAT);
                else return (Type.INT);
            if (b.op.RelationalOp( ) || b.op.BooleanOp( ))
                return (Type.BOOL);
        }
        if (e instanceof Unary) {
            Unary u = (Unary)e;
            if (u.op.NotOp( ))        return (Type.BOOL);
            else if (u.op.NegateOp( )) return typeOf(u.term,tm);
            else if (u.op.intOp( ))    return (Type.INT);
            else if (u.op.floatOp( )) return (Type.FLOAT);
            else if (u.op.charOp( ))  return (Type.CHAR);
        }
        throw new IllegalArgumentException("should never reach here");
    }

    public static void V (Expression e, TypeMap tm) {
        if (e instanceof Value)
            return;
        if (e instanceof Variable) {
            Variable v = (Variable)e;
            if(v.toString().contains("[")) {
                String tmp=v.toString();
                int index=tmp.indexOf("[");
                String tmp_change=tmp.substring(0,index);
                //String[] tmp_change = tmp.split(":");
                Variable tmp2 = new Variable(tmp_change);
                v=tmp2;
            }
            check( tm.containsKey(v)
                    , "undeclared variable: " + v);
            return;
        }
        if (e instanceof Binary) {
            Binary b = (Binary) e;
            Type typ1 = typeOf(b.term1, tm);
            Type typ2 = typeOf(b.term2, tm);
            V (b.term1, tm);
            V (b.term2, tm);
            boolean IsTrue1=false;
            boolean IsTrue2=false;
            if(typ1.toString().contains("[")&& typ1.toString().contains("]")){
                typ1.isArray=false;
                IsTrue1=true;
            }
            if(typ2.toString().contains("[")&& typ2.toString().contains("]")){
                typ2.isArray=false;
                IsTrue2=true;
            }

            if (b.op.ArithmeticOp( ))
                check( typ1.toString() == typ2.toString() &&
                                (typ1 == Type.INT || typ1 == Type.FLOAT)
                        ,  "해당 변수끼리는 "+b.op+" 연산이 불가능합니다.");
            else if (b.op.RelationalOp( ))
                check( typ1 == typ2 , "type error for " + b.op);
            else if (b.op.BooleanOp( ))
                check( typ1 == Type.BOOL && typ2 == Type.BOOL,
                        b.op + ": non-bool operand");
            else
                throw new IllegalArgumentException("should never reach here BinaryOp error");
            if(IsTrue1) typ1.isArray=true;
            if(IsTrue2) typ2.isArray=true;
            return;
        }
        if (e instanceof Unary) {
            Unary u = (Unary) e;
            Type type = typeOf(u.term, tm); //start here
            V(u.term, tm);
            if (u.op.NotOp()) {
                check((type == Type.BOOL), "type error for NotOp " + u.op);
            }
            else if (u.op.NegateOp()) {
                check((type == (Type.INT) || type == (Type.FLOAT)), "type error for NegateOp " + u.op);
            }
            else {
                throw new IllegalArgumentException("should never reach here UnaryOp error");
            }
            return;
        }

        throw new IllegalArgumentException("should never reach here");
    }

    public static void V (Statement s, TypeMap tm) {
        if ( s == null )
            throw new IllegalArgumentException( "AST error: null statement");
        else if (s instanceof Skip) return;
        else if (s instanceof Assignment) {
            Assignment a = (Assignment)s;
            Variable tmp3=a.target;
            Expression exp=a.source;
            String exp2=exp.toString();
            String tmp=tmp3.toString();
            System.out.println("test: "+tmp);

            if(tmp.contains("[")) {
                int index=tmp.indexOf("[");
                String tmp_change=tmp.substring(0,index);
                Variable tmp2 = new Variable(tmp_change);
                a.target=tmp2;
            }

            check( tm.containsKey(a.target)
                    , " undefined target in assignment: " + a.target);
            V(a.source, tm);
            Type ttype = (Type)tm.get(a.target); //ttype = target type; targets are only variables in Clite which are defined in the TypeMap
            Type srctype = typeOf(a.source, tm); //scrtype = source type; sources are Expressions or Statements which are not in the TypeMap
            System.out.println("타입은? " + ttype.toString());
            boolean IsTrue=false;
            if(ttype.toString().contains("[")&& ttype.toString().contains("]")){
                ttype.isArray=false;
                IsTrue=true;
            }
            if(srctype.toString().contains("[") && srctype.toString().contains("]")){
                srctype.isArray=false;
            }

            if (ttype.toString() != srctype.toString()) {
                if (ttype == Type.FLOAT)
                    check( srctype == Type.INT
                            , "mixed mode assignment to " + a.target);
                else if (ttype == Type.INT)
                    check( srctype == Type.CHAR
                            , "mixed mode assignment to " + a.target);

            }
            if(IsTrue) ttype.isArray=true;
            return;
        }
        else if (s instanceof Conditional) {
            Conditional c = (Conditional)s;
            V(c.test, tm);
            Type testtype = typeOf(c.test, tm);
            if (testtype == Type.BOOL) {
                V(c.thenbranch, tm);
                V(c.elsebranch, tm);
                return;
            }else {
                check( false, "poorly typed if in Conditional: " + c.test);
            }
        }
        else if (s instanceof Loop) {
            Loop l = (Loop)s;
            V(l.test, tm);
            Type testtype = typeOf(l.test, tm);
            if (testtype == Type.BOOL) {
                V(l.body, tm);
            }else {
                check ( false, "poorly typed test in while Loop in Conditional: " + l.test);
            }
        }
        else if (s instanceof Block) {
            Block b = (Block)s;
            for(Statement i : b.members) {
                V(i, tm);
            }

        }
        else if(s instanceof Scan){

        }
        else if(s instanceof Print){

        }else {
            throw new IllegalArgumentException("should never reach here");
        }
    }


} // class StaticTypeCheck