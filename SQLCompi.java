// Ramos Velasco Gabriel Antonio
// Sánchez Ortega Gabriel
// Chávez Cruz Adolfo
// Compiladores 5CV2

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.File;
import java.util.Arrays;
import java.util.List;

public class SQLCompi {

    public static void main(String[] args) {
        // Verificar si se pasaron al menos dos argumentos
        if (args.length < 1) {
            System.err.println("Uso: java SQLCompi <archivo_sql> <archivo_csv1> [<archivo_csv2> ...]");
            return;
        }

        String archivoSql = args[0]; // Primer argumento: archivo con la sentencia SQL
        String[] archivosCsv = Arrays.copyOfRange(args, 1, args.length); // Resto: archivos CSV

        try {
            // Leer la sentencia SQL
            String input = leerArchivo(archivoSql);
            if (input == null || input.trim().isEmpty()) {
                System.err.println("El archivo SQL está vacío.");
                return;
            }

            // Cargar los archivos CSV como tablas en la base de datos
            BaseDeDatos baseDeDatos = new BaseDeDatos();
            for (String archivoCsv : archivosCsv) {
                String nombreTabla = obtenerNombreTabla(archivoCsv);
                baseDeDatos.agregarTabla(nombreTabla, archivoCsv);
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
                ast.execute(baseDeDatos);

            } catch (RuntimeException e) {
                System.err.println("Error de sintaxis " + e.getMessage());
            }

        } catch (IOException e) {
            System.err.println("Error al procesar los archivos: " + e.getMessage());
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

    // Obtener el nombre de la tabla basado en el nombre del archivo CSV
    private static String obtenerNombreTabla(String archivoCsv) {
        File archivo = new File(archivoCsv);
        String nombre = archivo.getName();
        return nombre.substring(0, nombre.lastIndexOf('.')); // Elimina la extensión del archivo
    }
}
