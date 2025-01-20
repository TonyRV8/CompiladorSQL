import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

// Clase base para todas las declaraciones SQL
public abstract class Statement {
    public abstract void execute(BaseDeDatos baseDeDatos);
}

// Clase para manejar declaraciones SELECT
class SelectStatement extends Statement {
    private final List<Expr> columns;
    private final boolean isDistinct;

    public SelectStatement(List<Expr> columns, boolean isDistinct) {
        this.columns = columns;
        this.isDistinct = isDistinct;
    }

    public List<Expr> getColumns() {
        return columns;
    }

    public boolean isDistinct() {
        return isDistinct;
    }

    @Override
    public void execute(BaseDeDatos baseDeDatos) {
        for (Expr column : columns) {
            // Evaluar la expresión y capturar el resultado
            Object result = column.evaluate();

            // Imprimir el resultado de la evaluación
            System.out.println("Resultado: " + result);
        }
    }

    @Override
    public String toString() {
        return (isDistinct ? "DISTINCT " : "") + "SELECT " + columns;
    }
}

// Clase para manejar declaraciones FROM
class FromStatement extends Statement {
    private final List<String> tables;

    public FromStatement(List<String> tables) {
        this.tables = tables;
    }

    public List<String> getTables() {
        return tables;
    }

    @Override
    public void execute(BaseDeDatos baseDeDatos) {
        // Lógica para manejar las tablas en el FROM (vacío en este caso)
    }

    @Override
    public String toString() {
        return "FROM " + tables;
    }
}

// Clase para manejar declaraciones WHERE
class WhereStatement extends Statement {
    private final Expr condition;

    public WhereStatement(Expr condition) {
        this.condition = condition;
    }

    public Expr getCondition() {
        return condition;
    }

    @Override
    public void execute(BaseDeDatos baseDeDatos) {
        // Lógica para manejar la condición WHERE (vacío en este caso)
    }

    @Override
    public String toString() {
        return "WHERE " + condition;
    }
}

// Clase principal que integra SELECT, FROM y WHERE
class QueryStatement extends Statement {
    private final SelectStatement select;
    private final FromStatement from;
    private final WhereStatement where;

    public QueryStatement(SelectStatement select, FromStatement from, WhereStatement where) {
        this.select = select;
        this.from = from;
        this.where = where;
    }

    public SelectStatement getSelect() {
        return select;
    }

    public FromStatement getFrom() {
        return from;
    }

    public WhereStatement getWhere() {
        return where;
    }

    @Override
    public void execute(BaseDeDatos baseDeDatos) {
        if (from == null) {
            // Caso: No hay cláusula FROM (SELECT directo)
            ejecutarSelectDirecto();
        } else {
            // Caso: Cláusula FROM presente
            ejecutarConFrom(baseDeDatos);
        }
    }

    private void ejecutarSelectDirecto() {
        // Imprimir resultados de SELECT sin tablas
        for (Expr column : select.getColumns()) {
            Object resultado = column.evaluate();
            System.out.println(resultado);
        }
    }

    private void ejecutarConFrom(BaseDeDatos baseDeDatos) {
        // Verificar que haya tablas en FROM
        if (from.getTables().isEmpty()) {
            throw new RuntimeException("La consulta necesita al menos una tabla en la cláusula FROM.");
        }

        // Manejar el caso de producto cruz
        if (from.getTables().size() > 1) {
            ejecutarProductoCruz(baseDeDatos);
            return;
        }

        // Obtener la tabla de la base de datos
        String nombreTabla = from.getTables().get(0);
        BaseDeDatos.Tabla tabla = baseDeDatos.obtenerTabla(nombreTabla);
        if (tabla == null) {
            throw new RuntimeException("La tabla '" + nombreTabla + "' no existe.");
        }

        // Filtrar filas según la cláusula WHERE
        List<Map<String, String>> filas = tabla.getFilas();
        if (where != null && where.getCondition() != null) {
            filas = filtrarFilas(filas, where.getCondition());
        }

        // Seleccionar las columnas especificadas en SELECT
        imprimirResultados(filas, select, tabla.getColumnas());
    }

    private void ejecutarProductoCruz(BaseDeDatos baseDeDatos) {
        // Obtener las tablas involucradas en el producto cruz
        List<String> nombresTablas = from.getTables();
        BaseDeDatos.Tabla tabla1 = baseDeDatos.obtenerTabla(nombresTablas.get(0));
        BaseDeDatos.Tabla tabla2 = baseDeDatos.obtenerTabla(nombresTablas.get(1));

        if (tabla1 == null || tabla2 == null) {
            throw new RuntimeException("Una de las tablas especificadas no existe.");
        }

        // Generar el producto cruz
        List<Map<String, String>> filasProductoCruz = new ArrayList<>();
        for (Map<String, String> fila1 : tabla1.getFilas()) {
            for (Map<String, String> fila2 : tabla2.getFilas()) {
                Map<String, String> filaCombinada = new HashMap<>();
                // Combinar las columnas de ambas tablas
                for (String columna1 : tabla1.getColumnas()) {
                    filaCombinada.put(tabla1.getNombre() + "." + columna1, fila1.get(columna1));
                }
                for (String columna2 : tabla2.getColumnas()) {
                    filaCombinada.put(tabla2.getNombre() + "." + columna2, fila2.get(columna2));
                }
                filasProductoCruz.add(filaCombinada);
            }
        }

        // Seleccionar las columnas especificadas en SELECT
        List<String> columnasCombinadas = new ArrayList<>();
        for (String columna1 : tabla1.getColumnas()) {
            columnasCombinadas.add(tabla1.getNombre() + "." + columna1);
        }
        for (String columna2 : tabla2.getColumnas()) {
            columnasCombinadas.add(tabla2.getNombre() + "." + columna2);
        }

        imprimirResultados(filasProductoCruz, select, columnasCombinadas);
    }

    private List<Map<String, String>> filtrarFilas(List<Map<String, String>> filas, Expr condicion) {
        List<Map<String, String>> filasFiltradas = new ArrayList<>();
        for (Map<String, String> fila : filas) {
            if (evaluarCondicion(fila, condicion)) {
                filasFiltradas.add(fila);
            }
        }
        return filasFiltradas;
    }

    private boolean evaluarCondicion(Map<String, String> fila, Expr condicion) {
        if (condicion instanceof RelationalExpr) {
            RelationalExpr relExpr = (RelationalExpr) condicion;
            Object left = evaluarExprParaFila(fila, relExpr.left);
            Object right = evaluarExprParaFila(fila, relExpr.right);

            // Manejo de nulos
            if (left == null || right == null) {
                return false;
            }

            // Comparar valores
            switch (relExpr.operator) {
                case "=":
                    if (esNumero(left) && esNumero(right)) {
                        return Double.parseDouble(left.toString()) == Double.parseDouble(right.toString());
                    }
                    return left.toString().trim().equalsIgnoreCase(right.toString().trim());
                case "!=":
                    if (esNumero(left) && esNumero(right)) {
                        return Double.parseDouble(left.toString()) != Double.parseDouble(right.toString());
                    }
                    return !left.toString().trim().equalsIgnoreCase(right.toString().trim());
                case "<":
                    return Double.parseDouble(left.toString()) < Double.parseDouble(right.toString());
                case "<=":
                    return Double.parseDouble(left.toString()) <= Double.parseDouble(right.toString());
                case ">":
                    return Double.parseDouble(left.toString()) > Double.parseDouble(right.toString());
                case ">=":
                    return Double.parseDouble(left.toString()) >= Double.parseDouble(right.toString());
                default:
                    throw new UnsupportedOperationException("Operador no soportado: " + relExpr.operator);
            }
        }
        return false;
    }

    private Object evaluarExprParaFila(Map<String, String> fila, Expr expr) {
        if (expr instanceof IdExpr) {
            String nombreColumna = ((IdExpr) expr).id.toLowerCase(); // Normalizar el nombre a minúsculas
            return fila.getOrDefault(nombreColumna, null); // Usar el nombre normalizado
        } else if (expr instanceof ArithmeticExpr) {
            ArithmeticExpr arithExpr = (ArithmeticExpr) expr;
            Object left = evaluarExprParaFila(fila, arithExpr.left);
            Object right = evaluarExprParaFila(fila, arithExpr.right);

            if (left == null || right == null) {
                throw new RuntimeException("Error al evaluar la expresión: valores nulos en la operación.");
            }

            if (esNumero(left) && esNumero(right)) {
                double leftNum = Double.parseDouble(left.toString());
                double rightNum = Double.parseDouble(right.toString());

                switch (arithExpr.operator) {
                    case "+":
                        return leftNum + rightNum;
                    case "-":
                        return leftNum - rightNum;
                    case "*":
                        return leftNum * rightNum;
                    case "/":
                        if (rightNum == 0) {
                            throw new ArithmeticException("División por cero");
                        }
                        return leftNum / rightNum;
                    default:
                        throw new UnsupportedOperationException("Operador aritmético no soportado: " + arithExpr.operator);
                }
            } else {
                // Asignar valores para tipos no numéricos
                double leftVal = obtenerValorNumerico(left);
                double rightVal = obtenerValorNumerico(right);

                switch (arithExpr.operator) {
                    case "+":
                        return leftVal + rightVal;
                    case "-":
                        return leftVal - rightVal;
                    case "*":
                        return leftVal * rightVal;
                    case "/":
                        if (rightVal == 0) {
                            throw new ArithmeticException("División por cero");
                        }
                        return leftVal / rightVal;
                    default:
                        throw new UnsupportedOperationException("Operador aritmético no soportado: " + arithExpr.operator);
                }
            }
        } else if (expr instanceof FunctionCallExpr) {
            FunctionCallExpr funcExpr = (FunctionCallExpr) expr;
            String functionName = funcExpr.getFunctionName().toLowerCase();
            switch (functionName) {
                case "sum":
                    return calcularFuncionAgregada(fila, funcExpr, "sum");
                case "max":
                    return calcularFuncionAgregada(fila, funcExpr, "max");
                case "min":
                    return calcularFuncionAgregada(fila, funcExpr, "min");
                default:
                    throw new UnsupportedOperationException("Función no soportada: " + functionName);
            }
        }
        return expr.evaluate();
    }

    private double calcularFuncionAgregada(Map<String, String> fila, FunctionCallExpr funcExpr, String tipoFuncion) {
        String columna = funcExpr.getArguments().get(0).toString().toLowerCase();
        double resultado = tipoFuncion.equals("min") ? Double.MAX_VALUE : 0.0;

        for (String valor : fila.values()) {
            if (esNumero(valor)) {
                double valorNumerico = Double.parseDouble(valor);
                switch (tipoFuncion) {
                    case "sum":
                        resultado += valorNumerico;
                        break;
                    case "max":
                        resultado = Math.max(resultado, valorNumerico);
                        break;
                    case "min":
                        resultado = Math.min(resultado, valorNumerico);
                        break;
                }
            }
        }
        return resultado;
    }

    private double obtenerValorNumerico(Object valor) {
        if (valor instanceof Boolean) {
            return (Boolean) valor ? 1.0 : 0.0;
        } else if (valor instanceof String) {
            return 0.0; // Los strings valen 0
        } else if (esNumero(valor)) {
            return Double.parseDouble(valor.toString());
        } else {
            throw new RuntimeException("Tipo no soportado para la operación: " + valor);
        }
    }

    private boolean esNumero(Object valor) {
        if (valor == null) {
            return false;
        }
        try {
            Double.parseDouble(valor.toString());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void imprimirResultados(List<Map<String, String>> filas, SelectStatement select, List<String> columnasTabla) {
        List<Expr> columnasSelect = select.getColumns();
    
        // Si se selecciona "*", imprime todas las columnas
        if (columnasSelect.size() == 1 && columnasSelect.get(0) instanceof IdExpr &&
            ((IdExpr) columnasSelect.get(0)).id.equals("*")) {
            columnasSelect = new ArrayList<>();
            for (String columna : columnasTabla) {
                columnasSelect.add(new IdExpr(columna));
            }
        }
    
        // Variables para almacenar resultados de funciones de agregación
        Map<String, Double> resultadosAgregados = new HashMap<>();
    
        // Detectar funciones de agregación
        for (Expr columna : columnasSelect) {
            if (columna instanceof FunctionCallExpr) {
                FunctionCallExpr funcExpr = (FunctionCallExpr) columna;
                String functionName = funcExpr.getFunctionName().toLowerCase();
    
                if ("sum".equals(functionName) || "max".equals(functionName) || "min".equals(functionName)) {
                    String columnaNombre = funcExpr.getArguments().get(0).toString().toLowerCase();
                    double resultado = "min".equals(functionName) ? Double.MAX_VALUE : 0.0;
    
                    for (Map<String, String> fila : filas) {
                        String valor = fila.get(columnaNombre);
                        if (esNumero(valor)) {
                            double valorNumerico = Double.parseDouble(valor);
                            switch (functionName) {
                                case "sum":
                                    resultado += valorNumerico;
                                    break;
                                case "max":
                                    resultado = Math.max(resultado, valorNumerico);
                                    break;
                                case "min":
                                    resultado = Math.min(resultado, valorNumerico);
                                    break;
                            }
                        }
                    }
    
                    // Guardar el resultado de la función
                    resultadosAgregados.put(funcExpr.toString(), resultado);
                }
            }
        }
    
        // Si hay funciones de agregación, imprimir solo los resultados agregados
        if (!resultadosAgregados.isEmpty()) {
            System.out.println(String.join(", ", resultadosAgregados.keySet()));
            List<String> valores = new ArrayList<>();
            for (Double valor : resultadosAgregados.values()) {
                valores.add(valor.toString());
            }
            System.out.println(String.join(", ", valores));
            return; // Salir porque las funciones agregadas ya se imprimieron
        }
    
        // Si no hay funciones de agregación, imprimir las filas
        List<String> nombresColumnas = new ArrayList<>();
        for (Expr columna : columnasSelect) {
            nombresColumnas.add(columna.toString());
        }
        System.out.println(String.join(", ", nombresColumnas));
    
        // Evaluar y mostrar los resultados para cada fila
        for (Map<String, String> fila : filas) {
            List<String> valores = new ArrayList<>();
            for (Expr columna : columnasSelect) {
                try {
                    // Evaluar la expresión en el contexto de la fila actual
                    Object resultado = evaluarExprParaFila(fila, columna);
                    valores.add(resultado != null ? resultado.toString() : "null");
                } catch (Exception e) {
                    valores.add("ERROR: " + e.getMessage());
                }
            }
            System.out.println(String.join(", ", valores));
        }
    }
    

    @Override
    public String toString() {
        return "QueryStatement { " +
               "select=" + select +
               ", from=" + (from != null ? from : "null") +
               ", where=" + (where != null ? where : "null") +
               " }";
    }
}
