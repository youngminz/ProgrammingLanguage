package kr.youngminz.clitelexer;


class Parser {
    // Recursive descent parser that inputs a C++Lite program and 
    // generates its abstract syntax.  Each method corresponds to
    // a concrete syntax grammar rule, which appears as a comment
    // at the beginning of the method.

    private Token token;          // current token from the input stream
    private Lexer lexer;

    Parser(Lexer ts) { // Open the C++Lite source program
        lexer = ts;                          // as a token stream, and
        token = lexer.next();            // retrieve its first Token
    }

    private String match(TokenType t) { // * return the string of a token if it matches with t *
        String value = token.value();
        if (token.type().equals(t))
            token = lexer.next();
        else
            error(t);
        return value;
    }

    private void error(TokenType tok) {
        System.err.println("Syntax error: expecting: " + tok + "; saw: " + token);
        for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
            System.out.println(ste);
        }
        System.exit(1);
    }

    private void error(String tok) {
        System.err.println("Syntax error: expecting: " + tok + "; saw: " + token);
        for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
            System.out.println(ste);
        }
        System.exit(1);
    }

    public Program program() {
        match(TokenType.Decl);
        match(TokenType.LeftBrace);

        Declarations decls = declarations();

        match(TokenType.RightBrace);
        match(TokenType.Main);
        match(TokenType.LeftBrace);

        Block statements = new Block();
        while (!token.type().equals(TokenType.RightBrace)) {
            statements.members.add(statement());
        }

        match(TokenType.RightBrace);
        return new Program(decls, statements);
    }

    private Declarations declarations() {
        // Declarations --> { Declaration }
        Declarations declarations = new Declarations();
        while (token.type().equals(TokenType.Int) ||
                token.type().equals(TokenType.Bool) ||
                token.type().equals(TokenType.Float) ||
                token.type().equals(TokenType.Char)) {

            declaration(declarations);
        }

        return declarations;
    }

    private void declaration(Declarations ds) {
        // Declaration  --> Type : Identifier { , Identifier([Int]) } ;
        Type currentType = type();
        token = lexer.next();
        if (!token.type().equals(TokenType.Colon)) {
            error(TokenType.Colon);
        }
        // TODO 1차원 배열
        while (!token.type().equals(TokenType.Semicolon)) {
            token = lexer.next();
            if (token.type().equals(TokenType.Identifier)) {
                String token_value = token.value();
                token = lexer.next();
                if (token.type().equals(TokenType.LeftBracket)) {
                    token = lexer.next();
                    if (token.type().equals(TokenType.IntLiteral)) {
                        ds.add(new Declaration(new Variable(token_value), new Type(currentType.toString(), Integer.parseInt(token.value()))));
                    }
                    token = lexer.next();
                    if (!token.type().equals(TokenType.RightBracket)) {
                        error("]");
                    }
                    token = lexer.next();
                } else {
                    ds.add(new Declaration(new Variable(token_value), currentType));
                }
            } else {
                error("Identifier");
            }
            if (!token.type().equals(TokenType.Semicolon) && !token.type().equals(TokenType.Comma)) {
                error("; | ,");
            }
        }
        if (!token.type().equals(TokenType.Semicolon)) {
            error(TokenType.Semicolon);
        }
        token = lexer.next();
    }

    private Type type() {
        // Type  -->  int | bool | float | char
        if (token.type().equals(TokenType.Int)) {
            return Type.INT;
        } else if (token.type().equals(TokenType.Bool)) {
            return Type.BOOL;
        } else if (token.type().equals(TokenType.Float)) {
            return Type.FLOAT;
        } else if (token.type().equals(TokenType.Char)) {
            return Type.CHAR;
        } else {
            error("Type");
            return null;
        }
    }

    private Statement statement() {
        // Statement --> ; | Block | Assignment | IfStatement | WhileStatement
        if (token.type().equals(TokenType.Semicolon)) { // ;
            return new Skip();
        } else if (token.type().equals(TokenType.LeftBrace)) { // Block
            return statements();
        } else if (token.type().equals(TokenType.Identifier)) { // Assignment
            return assignment();
        } else if (token.type().equals(TokenType.If)) {
            return ifStatement();
        } else if (token.type().equals(TokenType.While)) {
            return whileStatement();
        } else if (token.type().equals(TokenType.Print)) {
            return printStatement();
        } else if (token.type().equals(TokenType.Scan)) {
            return scanStatement();
        } else {
            error("Statement");
            return null;
        }
    }

    private Block statements() {
        // Block --> '{' Statements '}'

        if (!token.type().equals(TokenType.LeftBrace)) {
            error(TokenType.LeftBrace);
        }
        token = lexer.next();
        Block b = new Block();
        while (!token.type().equals(TokenType.RightBrace)) {
            b.members.add(statement());
        }

        token = lexer.next();

        return b;
    }

    private Assignment assignment() {
        // Assignment --> Identifier = Expression ;
        String id = token.value();

        token = lexer.next();

        boolean isArray = false;
        Expression expr = null;
        if (token.type().equals(TokenType.LeftBracket)) {
            isArray = true;
            token = lexer.next();

            expr = expression();
            if (!token.type().equals(TokenType.RightBracket)) {
                error(TokenType.RightBracket);
            }
            token = lexer.next();
        }

        if (!token.type().equals(TokenType.Assign)) {
            error(TokenType.Assign);
        }
        token = lexer.next();

        Expression exp = expression();

        if (!token.type().equals(TokenType.Semicolon)) {
            error(TokenType.Assign);
        }
        token = lexer.next();

        if (isArray && expr != null) {
            return new Assignment(new Variable(id + "[" + expr.toString() + "]") , exp);
        }

        return new Assignment(new Variable(id), exp);
    }

    private Conditional ifStatement() {
        if (!token.type().equals(TokenType.If)) {
            error(TokenType.If);
        }

        token = lexer.next();
        if (!token.type().equals(TokenType.LeftParen)) {
            error(TokenType.LeftParen);
        }

        token = lexer.next();
        Expression exp = expression();

        if (!token.type().equals(TokenType.RightParen)) {
            error(TokenType.RightParen);
        }

        token = lexer.next();

        Statement state = statement();

        if (token.type().equals(TokenType.Else)) {
            token = lexer.next();
            Statement elseStatement = statement();

            return new Conditional(exp, state, elseStatement);
        }
        return new Conditional(exp, state);
    }

    private Loop whileStatement() {
        // WhileStatement --> while ( Expression ) Statement

        if (!token.type().equals(TokenType.While)) {
            error(TokenType.While);
        }

        token = lexer.next();
        if (!token.type().equals(TokenType.LeftParen)) {
            error(TokenType.LeftParen);
        }

        token = lexer.next();
        Expression exp = expression();

        if (!token.type().equals(TokenType.RightParen)) {
            error(TokenType.RightParen);
        }

        token = lexer.next();

        Statement state = statement();

        return new Loop(exp, state);
    }

    private Scan scanStatement() {
        if (!token.type().equals(TokenType.Scan)) {
            error(TokenType.Scan);
        }

        token = lexer.next();
        if (!token.type().equals(TokenType.LeftParen)) {
            error(TokenType.LeftParen);
        }

        token = lexer.next();
        if (!token.type().equals(TokenType.Identifier)) {
            error(TokenType.Identifier);
        }

        String varName = token.value();
        token = lexer.next();

        if (token.type().equals(TokenType.LeftBracket)) {
            token = lexer.next();
            if (!token.type().equals(TokenType.IntLiteral)) {
                error(TokenType.IntLiteral);
            }
            int index = Integer.parseInt(token.value());
            token = lexer.next();
            if (!token.type().equals(TokenType.RightBracket)) {
                error(TokenType.RightBracket);
            }
            varName = varName + ":" + Integer.toString(index);

            token = lexer.next();
        }

        if (!token.type().equals(TokenType.RightParen)) {
            error(TokenType.RightParen);
        }

        token = lexer.next();
        if (!token.type().equals(TokenType.Semicolon)) {
            error(TokenType.Semicolon);
        }
        token = lexer.next();

        return new Scan(new Variable(varName));
    }

    private Print printStatement() {
        if (!token.type().equals(TokenType.Print)) {
            error(TokenType.Print);
        }

        token = lexer.next();
        if (!token.type().equals(TokenType.LeftParen)) {
            error(TokenType.LeftParen);
        }

        token = lexer.next();
        Expression expr = expression();

        if (!token.type().equals(TokenType.RightParen)) {
            error(TokenType.RightParen);
        }

        token = lexer.next();
        if (!token.type().equals(TokenType.Semicolon)) {
            error(TokenType.Semicolon);
        }

        token = lexer.next();
        return new Print(expr);
    }

    private Expression expression() {
        // Expression --> Conjunction { || Conjunction }
        Expression lhs, rhs;

        lhs = conjunction();
        if (!token.type().equals(TokenType.Or)) {
            //token = lexer.next();
            return lhs;
        }
        token = lexer.next();

        rhs = conjunction();
        return new Binary(new Operator(Operator.OR), lhs, rhs);
    }

    private Expression conjunction() {
        // Conjunction --> Equality { && Equality }
        Expression lhs, rhs;

        lhs = equality();
        if (!token.type().equals(TokenType.And)) {
            //token = lexer.next();
            return lhs;
        }
        token = lexer.next();

        rhs = equality();
        return new Binary(new Operator(Operator.AND), lhs, rhs);
    }

    private Expression equality() {
        // Equality --> Relation [ EquOp Relation ]
        Expression lhs, rhs;

        lhs = relation();
        if (!isEqualityOp()) {
            //token = lexer.next();
            return lhs;
        }
        String op = token.value();
        token = lexer.next();

        rhs = relation();
        return new Binary(new Operator(op), lhs, rhs);
    }

    private Expression relation() {
        // Relation --> Addition [RelOp Addition]
        Expression lhs, rhs;

        lhs = addition();
        if (!isRelationalOp()) {
            //token = lexer.next();
            return lhs;
        }
        String op = token.value();
        token = lexer.next();

        rhs = addition();
        return new Binary(new Operator(op), lhs, rhs);
    }

    private Expression addition() {
        // Addition --> Term { AddOp Term }
        Expression e = term();
        while (isAddOp()) {
            Operator op = new Operator(match(token.type()));
            Expression term2 = term();
            e = new Binary(op, e, term2);
        }
        return e;
    }

    private Expression term() {
        // Term --> Factor { MultiplyOp Factor }
        Expression e = factor();
        while (isMultiplyOp()) {
            Operator op = new Operator(match(token.type()));
            Expression term2 = factor();
            e = new Binary(op, e, term2);
        }
        return e;
    }

    private Expression factor() {
        // Factor --> [ UnaryOp ] Primary
        if (isUnaryOp()) {
            Operator op = new Operator(match(token.type()));
            Expression term = primary();
            return new Unary(op, term);
        } else return primary();
    }

    private Expression primary() {
        // Primary --> Identifier | Literal | ( Expression )
        //             | Type ( Expression )
        Expression e = null;
        if (token.type().equals(TokenType.Identifier)) {
            //e = new Variable(match(TokenType.Identifier));
            String token_value = token.value();
            token = lexer.next();
            if (token.type().equals(TokenType.LeftBracket)) {
                token = lexer.next();
                Expression expr = expression();

                if (!token.type().equals(TokenType.RightBracket)){
                    error(TokenType.RightBracket);
                }
                token = lexer.next();
                e = new Variable(token_value + "[" + expr.toString() + "]");
            } else {
                e = new Variable(token_value);
            }
        } else if (isLiteral()) {
            e = literal();
        } else if (token.type().equals(TokenType.LeftParen)) {
            token = lexer.next();
            e = expression();
            match(TokenType.RightParen);
        } else if (isType()) {
            Operator op = new Operator(match(token.type()));
            match(TokenType.LeftParen);
            Expression term = expression();
            match(TokenType.RightParen);
            e = new Unary(op, term);
        } else error("Identifier | Literal | ( | Type");
        return e;
    }

    private Value literal() {
        if (token.type().equals(TokenType.IntLiteral)) {
            IntValue value = new IntValue(Integer.parseInt(token.value()));
            token = lexer.next();
            return value;
        } else if (token.type().equals(TokenType.CharLiteral)) {
            CharValue value = new CharValue(token.value().charAt(0));
            token = lexer.next();
            return value;
        } else if (token.type().equals(TokenType.FloatLiteral)) {
            FloatValue value = new FloatValue(Float.parseFloat(token.value()));
            token = lexer.next();
            return value;
        } else if (token.type().equals(TokenType.True)) {
            BoolValue value = new BoolValue(true);
            token = lexer.next();
            return value;
        } else if (token.type().equals(TokenType.False)) {
            BoolValue value = new BoolValue(false);
            token = lexer.next();
            return value;
        } else {
            error("Literal");
            return null;
        }
    }

    private boolean isAddOp() {
        return token.type().equals(TokenType.Plus) ||
                token.type().equals(TokenType.Minus);
    }

    private boolean isMultiplyOp() {
        return token.type().equals(TokenType.Multiply) ||
                token.type().equals(TokenType.Divide);
    }

    private boolean isUnaryOp() {
        return token.type().equals(TokenType.Not) ||
                token.type().equals(TokenType.Minus);
    }

    private boolean isEqualityOp() {
        return token.type().equals(TokenType.Equals) ||
                token.type().equals(TokenType.NotEqual);
    }

    private boolean isRelationalOp() {
        return token.type().equals(TokenType.Less) ||
                token.type().equals(TokenType.LessEqual) ||
                token.type().equals(TokenType.Greater) ||
                token.type().equals(TokenType.GreaterEqual);
    }

    private boolean isType() {
        return token.type().equals(TokenType.Int)
                || token.type().equals(TokenType.Bool)
                || token.type().equals(TokenType.Float)
                || token.type().equals(TokenType.Char);
    }

    private boolean isLiteral() {
        return token.type().equals(TokenType.IntLiteral) ||
                isBooleanLiteral() ||
                token.type().equals(TokenType.FloatLiteral) ||
                token.type().equals(TokenType.CharLiteral);
    }

    private boolean isBooleanLiteral() {
        return token.type().equals(TokenType.True) ||
                token.type().equals(TokenType.False);
    }

} // Parser
