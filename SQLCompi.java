// Ramos Velasco Gabriel Antonio
// Sánchez Ortega Gabriel
// Chávez Cruz Adolfo
// Compiladores 5CV2

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class SQLCompi {

    public static void main(String[] args) {
        // Verificar si se pasó al menos un argumento
        if (args.length < 1) {
            System.err.println("Falta poner la ruta del archivo>");
            return;
        }
    
        String filePath = args[0]; // Ruta del archivo pasada como argumento
    
        try {
            // Leer el archivo
            String input = leerArchivo(filePath);
            if (input == null) {
                System.err.println("No se pudo leer el archivo.");
                return;
            }
    
            // Generar tokens con el lexer
            List<AnalizadorLexico.Token> tokens = AnalizadorLexico.analizar(input);
    
            // Mostrar los tokens generados
            System.out.println("Tokens generados:");
            for (AnalizadorLexico.Token token : tokens) {
                System.out.println(token);
            }
    
            // Parsear los tokens con SQLParser
            SQLParser parser = new SQLParser(tokens);
            try {
                QueryStatement ast = parser.parseConsulta();
                System.out.println("La consulta es válida según la gramática definida.");
    
                // Imprimir el AST
                System.out.println("\nAST generado:");
                PrinterQuery.print(ast);
    
                // Ejecutar la consulta y mostrar resultados
                System.out.println("\nResultado de la consulta:");
                ast.execute();
    
            } catch (RuntimeException e) {
                System.err.println("Error de sintaxis: " + e.getMessage());
            }
    
        } catch (IOException e) {
            System.err.println("Error al leer el archivo: " + e.getMessage());
        }
    }
    

    // Método auxiliar para leer el contenido de un archivo
    private static String leerArchivo(String filePath) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                sb.append(linea).append("\n");
            }
        }
        return sb.toString();
    }
}
