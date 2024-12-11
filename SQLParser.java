//Ramos Velasco Gabriel Antonio
//Sánchez Ortega Gabriel
//Chávez Cruz Adolfo
//Compiladores 5CV2
import java.util.List;
import java.util.Iterator;

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

    public void parseConsulta() {
        match("SELECT");
        parseD();
        match("FROM");
        parseT();
        parseW();

        if (currentToken.tipo.equals("SEMICOLON")) {
            advance(); 
        }

        if (!currentToken.tipo.equals("EOF")) {
            throw new RuntimeException("Token inesperado " + currentToken);
        }
    }

    private void parseD() {
        if (currentToken.tipo.equals("DISTINCT")) {
            match("DISTINCT");
        }
        parseP();
    }

    private void parseP() {
        if (currentToken.tipo.equals("STAR")) {
            match("STAR");
        } else {
            parseF();
        }
    }

    private void parseF() {
        parseExpr();
        parseF1();
    }

    private void parseF1() {
        if (currentToken.tipo.equals("COMA")) {
            match("COMA");
            parseExpr();
            parseF1();
        }
    }

    private void parseT() {
        parseT1();
        parseT3();
    }

    private void parseT1() {
        match("IDENTIFICADOR"); // Expecting a table identifier
        parseT2();
    }

    private void parseT2() {
        if (currentToken.tipo.equals("IDENTIFICADOR")) {
            match("IDENTIFICADOR"); // table alias
        }
    }

    private void parseT3() {
        if (currentToken.tipo.equals("COMA")) {
            match("COMA");
            parseT();
        }
    }

     private void parseW() {
        if (currentToken.tipo.equals("WHERE")) {
            match("WHERE");
            parseExpr();
        }
    }
    

    private void parseExpr() {
        parseLogicOr();
    }

    private void parseLogicOr() {
        parseLogicAnd();
        while (currentToken.tipo.equals("OR")) {
            match("OR");
            parseLogicAnd();
        }
    }

    private void parseLogicAnd() {
        parseEquality();
        while (currentToken.tipo.equals("AND")) {
            match("AND");
            parseEquality();
        }
    }

    private void parseEquality() {
        parseComparison();
        if (currentToken.tipo.equals("EQ") || currentToken.tipo.equals("NE")) {
            match(currentToken.tipo);
            parseComparison();
        }
    }

    private void parseComparison() {
        parseTerm();
        if (currentToken.tipo.equals("LT") || currentToken.tipo.equals("LE") || 
            currentToken.tipo.equals("GT") || currentToken.tipo.equals("GE")) {
            match(currentToken.tipo);
            parseTerm();
        }
    }

    private void parseTerm() {
        parseFactor();
        while (currentToken.tipo.equals("PLUS") || currentToken.tipo.equals("MINUS")) {
            match(currentToken.tipo);
            parseFactor();
        }
    }

    private void parseFactor() {
        parseUnary();
        while(currentToken.tipo.equals("STAR") || currentToken.tipo.equals("SLASH")) {
            match(currentToken.tipo);
            parseUnary();
        }
    }

    private void parseUnary() {
        if (currentToken.tipo.equals("MINUS") || currentToken.tipo.equals("PLUS") || 
            currentToken.tipo.equals("NOT")) {
            match(currentToken.tipo);
        }
        parsePrimary();
    }

    private void parsePrimary() {
        switch (currentToken.tipo) {
            case "TRUE":
            case "FALSE":
            case "NULL":
            case "NUMERO":
            case "CADENA":
                match(currentToken.tipo);
                if (currentToken.tipo.equals("LEFT_PAREN")) {
                    parseCall();  // Llama a una función con argumentos
                } else {
                    parseAliasOpc();
                }
                break;
            case "IDENTIFICADOR":
                match("IDENTIFICADOR");
                if (currentToken.tipo.equals("LEFT_PAREN")) {
                    parseCall();  // Llama a una función con argumentos
                } else {
                    parseAliasOpc();
                }
                break;
            case "LEFT_PAREN":
                match("LEFT_PAREN");
                parseExpr();
                match("RIGHT_PAREN");
                break;
            default:
                throw new RuntimeException("Syntax error: Unexpected token " + currentToken);
        }
    }

    private void parseCall() {
        match("LEFT_PAREN");
        if (!currentToken.tipo.equals("RIGHT_PAREN")) {
            parseExpr(); // First argument if there's one
            while (currentToken.tipo.equals("COMA")) {
                match("COMA");
                parseExpr(); // Additional arguments if present
            }
        }
    
        // Match the closing parenthesis
        if (currentToken.tipo.equals("RIGHT_PAREN")) {
            match("RIGHT_PAREN");
        } 
    }

    private void parseAliasOpc() {
        if (currentToken.tipo.equals("DOT")) {
            match("DOT");
            parsePrimary();
        }
    }
}
