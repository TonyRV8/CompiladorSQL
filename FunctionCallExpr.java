import java.util.List;

public class FunctionCallExpr extends Expr {
    public final String functionName;
    public final List<Expr> arguments;

    public FunctionCallExpr(String functionName, List<Expr> arguments) {
        this.functionName = functionName;
        this.arguments = arguments;
    }

    @Override
    public String toString() {
        return "FunctionCallExpr: " + functionName + "(" + arguments + ")";
    }
}
