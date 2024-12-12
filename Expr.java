import java.util.List;

public abstract class Expr {
    public abstract String toString();
}

// Clases concretas para expresiones atómicas
class NumExpr extends Expr {
    public final double value;

    public NumExpr(double value) {
        this.value = value;
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
    public String toString() {
        return "\"" + value + "\"";
    }
}

class BooleanExpr extends Expr {
    public final boolean value;

    public BooleanExpr(boolean value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return Boolean.toString(value);
    }
}

// Clases para expresiones compuestas
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
    public String toString() {
        return functionName + "(" + arguments.toString() + ")";
    }
}
