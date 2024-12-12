//Ramos Velasco Gabriel Antonio
//Sánchez Ortega Gabriel
//Chávez Cruz Adolfo
//Compiladores 5CV2

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SQLParser {

    private Iterator<AnalizadorLexico.Token> tokens;
    private AnalizadorLexico.Token currentToken;

    public SQLParser(List<AnalizadorLexico.Token> tokens) {
        this.tokens = tokens.iterator();
        advance();
    }

    private void advance() {
        if (tokens.hasNext()) {
            currentToken = tokens.next();
        } else {
            currentToken = new AnalizadorLexico.Token("EOF", "$(EOF)", -1);
        }
    }

    private void match(String expectedType) {
        if (currentToken.tipo.equals(expectedType)) {
            advance();
        } else {
            throw new RuntimeException("Se esperaba " + expectedType + " pero se encontró " + currentToken);
        }
    }

    public QueryStatement parseConsulta() {
        match("SELECT");
        SelectStatement select = parseD();
        match("FROM");
        FromStatement from = parseT();
        WhereStatement where = parseW();

        if (currentToken.tipo.equals("SEMICOLON")) {
            advance();
        }

        if (!currentToken.tipo.equals("EOF")) {
            throw new RuntimeException("Token inesperado " + currentToken);
        }

        return new QueryStatement(select, from, where);
    }

    private SelectStatement parseD() {
        boolean isDistinct = false;
        if (currentToken.tipo.equals("DISTINCT")) {
            isDistinct = true;
            match("DISTINCT");
        }
        List<Expr> columns = parseP();
        return new SelectStatement(columns, isDistinct);
    }

    private List<Expr> parseP() {
        List<Expr> columns = new ArrayList<>();
        if (currentToken.tipo.equals("STAR")) {
            match("STAR");
            columns.add(new IdExpr("*"));
        } else {
            columns.add(parseF());
        }
        return columns;
    }

    private Expr parseF() {
        Expr expr = parseExpr();
        while (currentToken.tipo.equals("COMA")) {
            match("COMA");
            expr = new BinaryExpr(expr, ",", parseExpr());
        }
        return expr;
    }

    private FromStatement parseT() {
        List<String> tables = new ArrayList<>();
        do {
            tables.add(parseT1());
        } while (currentToken.tipo.equals("COMA") && advanceMatch("COMA"));
        return new FromStatement(tables);
    }

    private String parseT1() {
        String tableName = currentToken.valor;
        match("IDENTIFICADOR");
        if (currentToken.tipo.equals("IDENTIFICADOR")) {
            tableName += " " + currentToken.valor;
            match("IDENTIFICADOR");
        }
        return tableName;
    }

    private boolean advanceMatch(String expectedType) {
        if (currentToken.tipo.equals(expectedType)) {
            advance();
            return true;
        }
        return false;
    }

    private WhereStatement parseW() {
        if (currentToken.tipo.equals("WHERE")) {
            match("WHERE");
            Expr condition = parseExpr();
            return new WhereStatement(condition);
        }
        return null;
    }

    private Expr parseExpr() {
        return parseLogicOr();
    }

    private Expr parseLogicOr() {
        Expr left = parseLogicAnd();
        while (currentToken.tipo.equals("OR")) {
            match("OR");
            left = new BinaryExpr(left, "OR", parseLogicAnd());
        }
        return left;
    }

    private Expr parseLogicAnd() {
        Expr left = parseEquality();
        while (currentToken.tipo.equals("AND")) {
            match("AND");
            left = new BinaryExpr(left, "AND", parseEquality());
        }
        return left;
    }

    private Expr parseEquality() {
        Expr left = parseComparison();
        while (currentToken.tipo.equals("EQ") || currentToken.tipo.equals("NE")) {
            String operator = currentToken.tipo.equals("EQ") ? "=" : "!=";
            match(currentToken.tipo);
            left = new BinaryExpr(left, operator, parseComparison());
        }
        return left;
    }

    private Expr parseComparison() {
        Expr left = parseTerm();
        while (currentToken.tipo.equals("LT") || currentToken.tipo.equals("LE") ||
                currentToken.tipo.equals("GT") || currentToken.tipo.equals("GE")) {
            String operator = currentToken.tipo;
            match(currentToken.tipo);
            left = new BinaryExpr(left, operator, parseTerm());
        }
        return left;
    }

    private Expr parseTerm() {
        Expr left = parseFactor();
        while (currentToken.tipo.equals("PLUS") || currentToken.tipo.equals("MINUS")) {
            String operator = currentToken.tipo.equals("PLUS") ? "+" : "-";
            match(currentToken.tipo);
            left = new BinaryExpr(left, operator, parseFactor());
        }
        return left;
    }

    private Expr parseFactor() {
        Expr left = parseUnary();
        while (currentToken.tipo.equals("STAR") || currentToken.tipo.equals("SLASH")) {
            String operator = currentToken.tipo.equals("STAR") ? "*" : "/";
            match(currentToken.tipo);
            left = new BinaryExpr(left, operator, parseUnary());
        }
        return left;
    }

    private Expr parseUnary() {
        if (currentToken.tipo.equals("NOT") || currentToken.tipo.equals("MINUS")) {
            String operator = currentToken.tipo.equals("NOT") ? "NOT" : "-";
            match(currentToken.tipo);
            return new UnaryExpr(operator, parsePrimary());
        }
        return parsePrimary();
    }

    private Expr parsePrimary() {
        switch (currentToken.tipo) {
            case "TRUE":
                match("TRUE");
                return new BooleanExpr(true);
            case "FALSE":
                match("FALSE");
                return new BooleanExpr(false);
            case "NULL":
                match("NULL");
                return new IdExpr("NULL");
            case "NUMERO":
                String numero = currentToken.valor;
                match("NUMERO");
                return new NumExpr(Double.parseDouble(numero));
            case "CADENA":
                String cadena = currentToken.valor;
                match("CADENA");
                return new StringExpr(cadena);
            case "IDENTIFICADOR":
                String id = currentToken.valor;
                match("IDENTIFICADOR");
                Expr base = new IdExpr(id);
                if (currentToken.tipo.equals("LEFT_PAREN")) {
                    return parseCall(); // Function call
                }
                return parseAliasOpc(base); // Alias option
            case "LEFT_PAREN":
                match("LEFT_PAREN");
                Expr expr = parseExpr();
                match("RIGHT_PAREN");
                return expr;
            default:
                throw new RuntimeException("Syntax error: Unexpected token " + currentToken);
        }
    }
    

    private Expr parseCall() {
        match("LEFT_PAREN");
        List<Expr> arguments = new ArrayList<>();
        if (!currentToken.tipo.equals("RIGHT_PAREN")) {
            arguments.add(parseExpr()); // First argument if there's one
            while (currentToken.tipo.equals("COMA")) {
                match("COMA");
                arguments.add(parseExpr()); // Additional arguments if present
            }
        }
        match("RIGHT_PAREN"); // Match the closing parenthesis
        return new CallExpr("FunctionCall", arguments); // Example, replace "FunctionCall" with actual function name if available
    }

    private Expr parseAliasOpc(Expr base) {
        if (currentToken.tipo.equals("DOT")) {
            match("DOT");
            String alias = currentToken.valor;
            match("IDENTIFICADOR");
            return new BinaryExpr(base, ".", new IdExpr(alias));
        }
        return base; // No alias, return the base expression
    }
    
}
