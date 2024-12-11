//Ramos Velasco Gabriel Antonio
//Sánchez Ortega Gabriel
//Chávez Cruz Adolfo
//Compiladores 5CV2
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class SQLCompi {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Ingrese la ruta del archivo SQL: ");
        String filePath = scanner.nextLine();
        scanner.close();

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
                parser.parseConsulta();
                System.out.println("La consulta es válida según la gramática definida.");
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
