import java.util.List;

public abstract class Expr {
    public abstract Object evaluate();

    @Override
    public abstract String toString();
}

class NumExpr extends Expr {
    public final double value;

    public NumExpr(double value) {
        this.value = value;
    }

    @Override
    public Object evaluate() {
        return value;
    }

    @Override
    public String toString() {
        return Double.toString(value);
    }
}

class IdExpr extends Expr {
    public final String id;

    public IdExpr(String id) {
        this.id = id;
    }

    @Override
    public Object evaluate() {
        return null; // Depende del contexto, aquí se puede manejar como necesario.
    }

    @Override
    public String toString() {
        return id;
    }
}

class StringExpr extends Expr {
    public final String value;

    public StringExpr(String value) {
        this.value = value;
    }

    @Override
    public Object evaluate() {
        return 0; // Las cadenas se toman como 0 según tu especificación.
    }

    @Override
    public String toString() {
        return '"' + value + '"';
    }
}

class BooleanExpr extends Expr {
    public final boolean value;

    public BooleanExpr(boolean value) {
        this.value = value;
    }

    @Override
    public Object evaluate() {
        return value ? 1 : 0; // true -> 1, false -> 0
    }

    @Override
    public String toString() {
        return Boolean.toString(value);
    }
}

class BinaryExpr extends Expr {
    public final Expr left;
    public final String operator;
    public final Expr right;

    public BinaryExpr(Expr left, String operator, Expr right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    @Override
    public Object evaluate() {
        Object leftValue = left.evaluate();
        Object rightValue = right.evaluate();

        if (leftValue instanceof Number && rightValue instanceof Number) {
            double leftNum = ((Number) leftValue).doubleValue();
            double rightNum = ((Number) rightValue).doubleValue();

            switch (operator) {
                case "+":
                    return leftNum + rightNum;
                case "-":
                    return leftNum - rightNum;
                case "*":
                    return leftNum * rightNum;
                case "/":
                    if (rightNum == 0) throw new ArithmeticException("Division by zero");
                    return leftNum / rightNum;
                default:
                    throw new IllegalArgumentException("Unknown operator: " + operator);
            }
        }

        return 0; // Si los operandos no son números, retorna 0 por defecto.
    }

    @Override
    public String toString() {
        return "(" + left + " " + operator + " " + right + ")";
    }
}

class UnaryExpr extends Expr {
    public final String operator;
    public final Expr expr;

    public UnaryExpr(String operator, Expr expr) {
        this.operator = operator;
        this.expr = expr;
    }

    @Override
    public Object evaluate() {
        Object value = expr.evaluate();
        if (value instanceof Number) {
            double num = ((Number) value).doubleValue();
            return operator.equals("-") ? -num : num;
        }

        return 0; // Por defecto, para expresiones no numéricas.
    }

    @Override
    public String toString() {
        return operator + expr.toString();
    }
}

class CallExpr extends Expr {
    public final String functionName;
    public final List<Expr> arguments;

    public CallExpr(String functionName, List<Expr> arguments) {
        this.functionName = functionName;
        this.arguments = arguments;
    }

    @Override
    public Object evaluate() {
        // Implementar lógica de funciones si es necesario
        return null;
    }

    @Override
    public String toString() {
        return functionName + "(" + arguments.toString() + ")";
    }
}

class LiteralExpr extends Expr {
    public final Object value;

    public LiteralExpr(Object value) {
        this.value = value;
    }

    @Override
    public Object evaluate() {
        return value;
    }

    @Override
    public String toString() {
        return "LiteralExpr: " + value;
    }
}

class ArithmeticExpr extends Expr {
    public final Expr left;
    public final String operator;
    public final Expr right;

    public ArithmeticExpr(Expr left, String operator, Expr right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    @Override
public Object evaluate() {
    Object leftValue = left.evaluate();
    Object rightValue = right.evaluate();

    double leftNum = 0.0;
    double rightNum = 0.0;

    // Convertir valores a números
    if (leftValue instanceof Number) {
        leftNum = ((Number) leftValue).doubleValue();
    } else if (leftValue instanceof Boolean) {
        leftNum = (Boolean) leftValue ? 1.0 : 0.0; // true = 1, false = 0
    } else if (leftValue instanceof String) {
        leftNum = 0.0; // Las cadenas se evalúan como 0
    }

    if (rightValue instanceof Number) {
        rightNum = ((Number) rightValue).doubleValue();
    } else if (rightValue instanceof Boolean) {
        rightNum = (Boolean) rightValue ? 1.0 : 0.0; // true = 1, false = 0
    } else if (rightValue instanceof String) {
        rightNum = 0.0; // Las cadenas se evalúan como 0
    }

    // Realizar la operación aritmética
    switch (operator) {
        case "+":
            return leftNum + rightNum;
        case "-":
            return leftNum - rightNum;
        case "*":
            return leftNum * rightNum;
        case "/":
            if (rightNum == 0) throw new ArithmeticException("División entre zero");
            return leftNum / rightNum;
        default:
            throw new IllegalArgumentException("Unknown operator: " + operator);
    }
}



    @Override
    public String toString() {
        return "(" + left + " " + operator + " " + right + ")";
    }
}

class RelationalExpr extends Expr {
    public final Expr left;
    public final String operator;
    public final Expr right;

    public RelationalExpr(Expr left, String operator, Expr right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    @Override
    public Object evaluate() {
        Object leftValue = left.evaluate();
        Object rightValue = right.evaluate();

        if (leftValue instanceof Comparable && rightValue instanceof Comparable) {
            @SuppressWarnings("unchecked")
            Comparable<Object> leftComp = (Comparable<Object>) leftValue;

            switch (operator) {
                case "<":
                    return leftComp.compareTo(rightValue) < 0;
                case "<=":
                    return leftComp.compareTo(rightValue) <= 0;
                case ">":
                    return leftComp.compareTo(rightValue) > 0;
                case ">=":
                    return leftComp.compareTo(rightValue) >= 0;
                case "=":
                    return leftComp.equals(rightValue);
                case "!=":
                    return !leftComp.equals(rightValue);
                default:
                    throw new IllegalArgumentException("Unknown operator: " + operator);
            }
        }

        return false;
    }

    @Override
    public String toString() {
        return "(" + left + " " + operator + " " + right + ")";
    }
}

class TableExpr extends Expr {
    public final String tableName;
    private final String alias;

    public TableExpr(String tableName, String alias) {
        this.tableName = tableName;
        this.alias = alias;
    }

    @Override
    public Object evaluate() {
        // La evaluación de tablas podría ser opcional.
        return null;
    }

    @Override
    public String toString() {
        return alias != null ? tableName + " AS " + alias : tableName;
    }
}
