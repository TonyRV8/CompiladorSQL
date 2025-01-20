import java.util.List;

public class FunctionCallExpr extends Expr {
    private final String functionName;
    private final List<Expr> arguments;

    public FunctionCallExpr(String functionName, List<Expr> arguments) {
        this.functionName = functionName;
        this.arguments = arguments;
    }

    public String getFunctionName() {
        return functionName;
    }

    public List<Expr> getArguments() {
        return arguments;
    }

    @Override
    public Object evaluate() {
        // Implementa la lógica para evaluar funciones si es necesario.
        // Aquí se retorna null como placeholder.
        return null;
    }

    @Override
    public String toString() {
        return functionName + "(" + arguments + ")";
    }
}
