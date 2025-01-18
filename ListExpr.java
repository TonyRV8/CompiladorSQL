import java.util.List;

public class ListExpr extends Expr {
    private final String operator;  // Operador: "AND", "OR", ","
    private final List<Expr> expressions;

    public ListExpr(String operator, List<Expr> expressions) {
        this.operator = operator;
        this.expressions = expressions;
    }
    
    public String getOperator() {  // Getter para operator
        return operator;
    }

    public List<Expr> getExpressions() {  // Getter para expressions
        return expressions;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(operator + ": [");
        for (int i = 0; i < expressions.size(); i++) {
            sb.append(expressions.get(i));
            if (i < expressions.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}
