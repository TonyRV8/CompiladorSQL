//Chávez Cruz Adolfo
//Ramos Velasco Gabriel Antonio
//Sánchez Ortega Gabriel
//Compiladores 5CV2

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class AnalizadorLexico {

    // Palabras reservadas de SQL (case insensitive)
    private static final String[] PALABRAS_RESERVADAS = {
        "and", "distinct", "false", "from", "is", "not", "null", "or", "select", "true", "where"
    };

    // Operadores relacionales
    private static final String[] OPERADORES_RELACIONALES = {"<", "<=", ">", ">=", "=", "<>"};

    // Operadores aritméticos
    private static final char[] OPERADORES_ARITMETICOS = {'+', '-', '*', '/'};

    // Signos de puntuación
    private static final char[] SIGNOS = {',', ';', '.', '(', ')'};

    // Estructura que representa un token
    public static class Token {
        public String tipo;
        public String valor;
        public int posicionInicio;

        public Token(String tipo, String valor, int posicionInicio) {
            this.tipo = tipo;
            this.valor = valor;
            this.posicionInicio = posicionInicio;
        }

        @Override
        public String toString() {
            return String.format("<%s, %s, posición: %d>", tipo, valor, posicionInicio);
        }
    }

    // Método principal para analizar la entrada SQL
    public static List<Token> analizar(String input) {
        List<Token> tokens = new ArrayList<>();
        int i = 0;
        boolean enComentarioLinea = false;
        boolean enComentarioMultilinea = false;

        while (i < input.length()) {
            char c = input.charAt(i);

            // Ignorar comentarios de una línea (-- comentario)
            if (!enComentarioMultilinea && i + 1 < input.length() && input.charAt(i) == '-' && input.charAt(i + 1) == '-') {
                enComentarioLinea = true;
            }
            if (enComentarioLinea && c == '\n') {
                enComentarioLinea = false;
            }
            if (enComentarioLinea) {
                i++;
                continue;
            }

            // Ignorar comentarios multilínea (/* comentario */)
            if (!enComentarioMultilinea && i + 1 < input.length() && input.charAt(i) == '/' && input.charAt(i + 1) == '*') {
                enComentarioMultilinea = true;
                i += 2;
                continue;
            }
            if (enComentarioMultilinea && i + 1 < input.length() && input.charAt(i) == '*' && input.charAt(i + 1) == '/') {
                enComentarioMultilinea = false;
                i += 2;
                continue;
            }
            if (enComentarioMultilinea) {
                i++;
                continue;
            }

            // Ignorar espacios en blanco
            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }

            int posicionInicio = i;

            // Palabras reservadas o identificadores (insensible a mayúsculas)
            if (Character.isLetter(c) || c == '_') {
                StringBuilder sb = new StringBuilder();
                while (i < input.length() && (Character.isLetterOrDigit(input.charAt(i)) || input.charAt(i) == '_')) {
                    sb.append(input.charAt(i));
                    i++;
                }
                String palabra = sb.toString().toLowerCase(); // Convertir a minúsculas
                if (esPalabraReservada(palabra)) {
                    tokens.add(new Token(palabra.toUpperCase(), palabra, posicionInicio));
                } else {
                    tokens.add(new Token("IDENTIFICADOR", sb.toString(), posicionInicio));
                }
                continue;
            }

            // Números
            if (Character.isDigit(c)) {
                StringBuilder sb = new StringBuilder();
                boolean tienePuntoDecimal = false;
                boolean tieneExp = false;

                while (i < input.length() && (Character.isDigit(input.charAt(i)) || input.charAt(i) == '.' || 
                       input.charAt(i) == 'e' || input.charAt(i) == 'E')) {

                    if (input.charAt(i) == 'e' || input.charAt(i) == 'E') {
                        if (tieneExp) {
                            System.out.println("Error: Número contiene más de un símbolo exponencial.");
                            return tokens; 
                        }
                        tieneExp = true;
                        sb.append(input.charAt(i));
                        i++;
                        if (i < input.length() && (input.charAt(i) == '+' || input.charAt(i) == '-')) {
                            sb.append(input.charAt(i)); // Manejar signo después del exponente
                            i++;
                        }
                        continue;
                    }

                    if (input.charAt(i) == '.') {
                        if (tienePuntoDecimal) {
                            System.out.println("Error: Número contiene más de un punto decimal.");
                            return tokens;
                        }
                        tienePuntoDecimal = true;
                    }

                    sb.append(input.charAt(i));
                    i++;
                }
                tokens.add(new Token("NUMERO", sb.toString(), posicionInicio));
                continue;
            }

            // Cadenas
            if (c == '\'' || c == '\"') {
                i++;
                StringBuilder sb = new StringBuilder();
                while (i < input.length() && input.charAt(i) != '\'' && input.charAt(i) != '\"') {
                    sb.append(input.charAt(i));
                    i++;
                }
                i++; 
                tokens.add(new Token("CADENA", "'" + sb.toString() + "'", posicionInicio));
                continue;
            }

            // Operadores aritméticos
            if (esOperadorAritmetico(c)) {
                tokens.add(new Token(getTokenOperadorAritmetico(c), String.valueOf(c), posicionInicio));
                i++;
                continue;
            }

            // Operadores relacionales
            if (esOperadorRelacional(c)) {
                StringBuilder sb = new StringBuilder();
                sb.append(c);
                i++;
                if (i < input.length() && (input.charAt(i) == '=' || (c == '<' && input.charAt(i) == '>'))) {
                    sb.append(input.charAt(i));
                    i++;
                }
                tokens.add(new Token(getTokenOperadorRelacional(sb.toString()), sb.toString(), posicionInicio));
                continue;
            }

            // Signos de puntuación
            if (esSigno(c)) {
                tokens.add(new Token(getTokenSigno(c), String.valueOf(c), posicionInicio));
                i++;
                continue;
            }

            i++;
        }

        tokens.add(new Token("EOF", "$(EOF)", i));
        return tokens;
    }

    // Método auxiliar para verificar si es una palabra reservada (insensible a mayúsculas)
    private static boolean esPalabraReservada(String palabra) {
        for (String reservada : PALABRAS_RESERVADAS) {
            if (reservada.equals(palabra.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    // Método auxiliar para verificar si es un operador aritmético
    private static boolean esOperadorAritmetico(char c) {
        for (char op : OPERADORES_ARITMETICOS) {
            if (op == c) {
                return true;
            }
        }
        return false;
    }

    // Método auxiliar para verificar si es un operador relacional
    private static boolean esOperadorRelacional(char c) {
        for (String op : OPERADORES_RELACIONALES) {
            if (op.charAt(0) == c) {
                return true;
            }
        }
        return false;
    }

    // Método auxiliar para verificar si es un signo de puntuación
    private static boolean esSigno(char c) {
        for (char s : SIGNOS) {
            if (s == c) {
                return true;
            }
        }
        return false;
    }

    // Obtener el tipo de token para operadores aritméticos
    private static String getTokenOperadorAritmetico(char c) {
        switch (c) {
            case '+': return "PLUS";
            case '-': return "MINUS";
            case '*': return "STAR";
            case '/': return "SLASH";
            default: return "OPERADOR_ARITMETICO";
        }
    }

    // Obtener el tipo de token para operadores relacionales
    private static String getTokenOperadorRelacional(String op) {
        switch (op) {
            case "=": return "EQ";
            case "<": return "LT";
            case "<=": return "LE";
            case ">": return "GT";
            case ">=": return "GE";
            case "<>": return "NE";
            default: return "OPERADOR_RELACIONAL";
        }
    }

    // Obtener el tipo de token para signos de puntuación
    private static String getTokenSigno(char c) {
        switch (c) {
            case ',': return "COMA";
            case ';': return "SEMICOLON";
            case '.': return "DOT";
            case '(': return "LEFT_PAREN";
            case ')': return "RIGHT_PAREN";
            default: return "SIGNO";
        }
    }

    // Método principal que abre un archivo y analiza su contenido
    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        System.out.print("Ingrese la ruta del archivo: ");
        String filePath = scanner.nextLine();
        scanner.close();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            StringBuilder sb = new StringBuilder();
            String linea;
            while ((linea = br.readLine()) != null) {
                sb.append(linea).append("\n");
            }
            String input = sb.toString();
            List<Token> tokens = analizar(input);

            // Imprimir los tokens resultantes
            for (Token token : tokens) {
                System.out.println(token);
            }
        } catch (IOException e) {
            System.err.println("Error al leer el archivo: " + e.getMessage());
    }
    }
}
