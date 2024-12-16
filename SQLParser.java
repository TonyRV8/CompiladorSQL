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
            columns.addAll(parseF());
        }
        return columns;
    }

    private List<Expr> parseF() {
        List<Expr> expressions = new ArrayList<>();
        expressions.add(parseExpr());
        while (currentToken.tipo.equals("COMA")) {
            match("COMA");
            expressions.add(parseExpr());
        }
        return expressions;
    }

    private FromStatement parseT() {
        List<String> tables = new ArrayList<>();
        
        parseT1(tables);  // Maneja la primera tabla
        parseT3(tables); 
        return new FromStatement(tables);
    }

    private void parseT1(List<String> tables) {
        String tableName = currentToken.valor;
        match("IDENTIFICADOR");  // Nombre de la tabla
    
        String alias = parseT2();  // Alias opcional
        if (alias != null) {
            tables.add(tableName + " AS " + alias);  // Añade la tabla con alias
        } else {
            tables.add(tableName);  // Añade solo el nombre de la tabla
        }
    }
    

    private String parseT2() {
        if (currentToken.tipo.equals("IDENTIFICADOR")) {
            String alias = currentToken.valor;
            match("IDENTIFICADOR");  // Alias de la tabla
            return alias;
        }
        return null;  // No hay alias
    }
    
    private void parseT3(List<String> tables) {
            while (currentToken.tipo.equals("COMA")) {
                match("COMA");
                parseT1(tables);  // Repite para la siguiente tabla
        }
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
        List<Expr> expressions = new ArrayList<>();
        expressions.add(parseLogicAnd());
        while (currentToken.tipo.equals("OR")) {
            match("OR");
            expressions.add(parseLogicAnd());
        }
        if (expressions.size() == 1) {
            return expressions.get(0);  // Retorna una sola expresión si solo hay una
        }
        return new ListExpr("OR", expressions);  // Retorna ListExpr si hay múltiples
    }
    

    private Expr parseLogicAnd() {
        List<Expr> expressions = new ArrayList<>();
        expressions.add(parseEquality());
        while (currentToken.tipo.equals("AND")) {
            match("AND");
            expressions.add(parseEquality());
        }
        if (expressions.size() == 1) {
            return expressions.get(0);  // Retorna una sola expresión si solo hay una
        }
        return new ListExpr("AND", expressions);  // Retorna ListExpr si hay múltiples
    }
    

    private Expr parseEquality() {
        Expr left = parseComparison();
        while (currentToken.tipo.equals("EQ") || currentToken.tipo.equals("NE")) {
            String operator = currentToken.tipo.equals("EQ") ? "=" : "!=";
            match(currentToken.tipo);
            left = new RelationalExpr(left, operator, parseComparison());
        }
        return left;
    }

    private Expr parseComparison() {
        Expr left = parseTerm();
        while (currentToken.tipo.equals("LT") || currentToken.tipo.equals("LE") ||
                currentToken.tipo.equals("GT") || currentToken.tipo.equals("GE")) {
            String operator = currentToken.valor;
            match(currentToken.tipo);
            left = new RelationalExpr(left, operator, parseTerm());
        }
        return left;
    }

    private Expr parseTerm() {
        Expr left = parseFactor();
        while (currentToken.tipo.equals("PLUS") || currentToken.tipo.equals("MINUS")) {
            String operator = currentToken.valor;
            match(currentToken.tipo);
            left = new ArithmeticExpr(left, operator, parseFactor());
        }
        return left;
    }

    private Expr parseFactor() {
        Expr left = parseUnary();
        while (currentToken.tipo.equals("STAR") || currentToken.tipo.equals("SLASH")) {
            String operator = currentToken.valor;
            match(currentToken.tipo);
            left = new ArithmeticExpr(left, operator, parseUnary());
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
                return new LiteralExpr(true);
            case "FALSE":
                match("FALSE");
                return new LiteralExpr(false);
            case "NULL":
                match("NULL");
                return new LiteralExpr(null);
            case "NUMERO":
                String numero = currentToken.valor;
                match("NUMERO");
                return new LiteralExpr(Double.parseDouble(numero));
            case "CADENA":
                String cadena = currentToken.valor;
                match("CADENA");
                Expr base = new LiteralExpr(cadena);
                if (currentToken.tipo.equals("LEFT_PAREN")) {
                    return parseCall(base); // Llama a una función con argumentos
                }
                return parseAliasOpc(base); // Maneja alias opcionales
            case "IDENTIFICADOR":
                String id = currentToken.valor;
                match("IDENTIFICADOR");
                Expr baseId = new IdExpr(id);
                if (currentToken.tipo.equals("LEFT_PAREN")) {
                    return parseCall(baseId); // Llama a una función con argumentos
                }
                return parseAliasOpc(baseId); // Maneja alias opcionales
            case "LEFT_PAREN":
                match("LEFT_PAREN");
                Expr expr = parseExpr();
                match("RIGHT_PAREN");
                return expr;
            default:
                throw new RuntimeException("Syntax error: Unexpected token " + currentToken);
        }
    }
    

    private Expr parseCall(Expr base) {
        match("LEFT_PAREN");
        List<Expr> arguments = new ArrayList<>();
        
        if (!currentToken.tipo.equals("RIGHT_PAREN")) {
            arguments.add(parseExpr()); // Primer argumento
            while (currentToken.tipo.equals("COMA")) {
                match("COMA");
                arguments.add(parseExpr()); // Argumentos adicionales
            }
        }
        match("RIGHT_PAREN");
    
        if (base instanceof IdExpr) {
            return new FunctionCallExpr(((IdExpr) base).id, arguments); // Explicita que es una función
        } else {
            throw new RuntimeException("Error de sintaxis: La llamada a función es inválida.");
        }
    }
    
    

    private Expr parseAliasOpc(Expr base) {
        if (currentToken.tipo.equals("DOT")) { 
            match("DOT"); // Coincide con el punto
            if (currentToken.tipo.equals("IDENTIFICADOR")) {
                String field = currentToken.valor;
                match("IDENTIFICADOR");
                Expr newBase = new IdExpr(base.toString() + "." + field);
                
                // Si después del identificador hay un paréntesis, es una llamada a función
                if (currentToken.tipo.equals("LEFT_PAREN")) {
                    return parseCall(newBase);
                }
                return newBase; // Devuelve col1.a o csv.sum
            } else {
                throw new RuntimeException("Error de sintaxis: Se esperaba un identificador después de '.'");
            }
        }
        // Si hay paréntesis después de un identificador, llama a la función
        if (currentToken.tipo.equals("LEFT_PAREN")) {
            return parseCall(base);
        }
        return base; // Si no hay punto ni paréntesis, devuelve el identificador base
    }
    
    
    
}