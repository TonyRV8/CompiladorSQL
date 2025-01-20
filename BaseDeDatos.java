import java.io.*;
import java.util.*;

public class BaseDeDatos {

    // Clase para representar una tabla cargada desde un archivo CSV
    public static class Tabla {
        private String nombre; // Nombre de la tabla
        private List<String> columnas; // Nombres de las columnas
        private List<Map<String, String>> filas; // Lista de filas (cada fila es un mapa columna-valor)

        public Tabla(String nombre, String rutaCsv) throws IOException {
            this.nombre = nombre;
            cargarDesdeCsv(rutaCsv);
        }

        // Carga los datos desde un archivo CSV
        private void cargarDesdeCsv(String rutaCsv) throws IOException {
            filas = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new FileReader(rutaCsv))) {
                String linea;
        
                // Leer la primera línea (nombres de las columnas)
                if ((linea = br.readLine()) != null) {
                    // Eliminar BOM y caracteres invisibles
                    linea = linea.replaceAll("[^\\x20-\\x7E]", "").trim();
                    columnas = Arrays.asList(linea.split(","));
                } else {
                    throw new IOException("El archivo CSV está vacío: " + rutaCsv);
                }
        
                // Leer las filas de datos
                while ((linea = br.readLine()) != null) {
                    String[] valores = linea.split(",");
                    Map<String, String> fila = new HashMap<>();
                    for (int i = 0; i < columnas.size(); i++) {
                        fila.put(columnas.get(i), i < valores.length ? valores[i].trim() : null);
                    }
                    filas.add(fila);
                }
            }
        }
        
        


        public String getNombre() {
            return nombre;
        }

        public List<String> getColumnas() {
            return columnas;
        }

        public List<Map<String, String>> getFilas() {
            return filas;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("Tabla: " + nombre + "\n");
            sb.append("Columnas: ").append(columnas).append("\n");
            sb.append("Filas:\n");
            for (Map<String, String> fila : filas) {
                sb.append("  ").append(fila).append("\n");
            }
            return sb.toString();
        }
    }

    // Clase para administrar varias tablas
    private Map<String, Tabla> tablas;

    public BaseDeDatos() {
        tablas = new HashMap<>();
    }

    // Agrega una tabla desde un archivo CSV
    public void agregarTabla(String nombre, String rutaCsv) throws IOException {
        tablas.put(nombre, new Tabla(nombre, rutaCsv));
    }

    // Obtiene una tabla por su nombre
    public Tabla obtenerTabla(String nombre) {
        return tablas.get(nombre);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Base de Datos:\n");
        for (Tabla tabla : tablas.values()) {
            sb.append(tabla).append("\n");
        }
        return sb.toString();
    }
}
