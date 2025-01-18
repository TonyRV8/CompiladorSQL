import java.util.List;

public class PrinterQuery {

    public static void print(QueryStatement stmt) {
        System.out.println("* Query:");

        // Imprimir SELECT
        System.out.println("  - Select:");
        for (Expr column : stmt.getSelect().getColumns()) {
            System.out.println("      - " + printExpr(column));
        }

        // Imprimir FROM
        System.out.println("  - From:");
        if (stmt.getFrom() != null) {
            for (String table : stmt.getFrom().getTables()) {
                System.out.println("      - TableExpr: " + table);
            }
        } else {
            System.out.println("      - null");
        }

        // Imprimir WHERE
        System.out.println("  - Where:");
        if (stmt.getWhere() != null) {
            System.out.println("      - " + printExpr(stmt.getWhere().getCondition()));
        } else {
            System.out.println("      - null");
        }
    }

    private static String printExpr(Expr expr) {
        if (expr instanceof LiteralExpr) {
            return "LiteralExpr: " + ((LiteralExpr) expr).value;
        } else if (expr instanceof ArithmeticExpr) {
            ArithmeticExpr arithmeticExpr = (ArithmeticExpr) expr;
            return "ArithmeticExpr: " + printExpr(arithmeticExpr.left) + " " + arithmeticExpr.operator + " "
                    + printExpr(arithmeticExpr.right);
        } else if (expr instanceof RelationalExpr) {
            RelationalExpr relationalExpr = (RelationalExpr) expr;
            return "RelationalExpr: " + printExpr(relationalExpr.left) + " " + relationalExpr.operator + " "
                    + printExpr(relationalExpr.right);
        } else if (expr instanceof UnaryExpr) {
            UnaryExpr unaryExpr = (UnaryExpr) expr;
            return "UnaryExpr: " + unaryExpr.operator + " " + printExpr(unaryExpr.expr);
        } else if (expr instanceof CallExpr) {
            CallExpr callExpr = (CallExpr) expr;
            return "CallExpr: " + callExpr.functionName + "(" + printArguments(callExpr.arguments) + ")";
        } else if (expr instanceof IdExpr) {
            return "IdExpr: " + ((IdExpr) expr).id;
        } else if (expr instanceof TableExpr) {
            return "TableExpr: " + ((TableExpr) expr).tableName;
        } else if (expr instanceof ListExpr) {
            ListExpr listExpr = (ListExpr) expr;
            StringBuilder sb = new StringBuilder();
            sb.append(listExpr.getOperator()).append(": [");
            for (int i = 0; i < listExpr.getExpressions().size(); i++) {
                sb.append(printExpr(listExpr.getExpressions().get(i)));
                if (i < listExpr.getExpressions().size() - 1) {
                    sb.append(", ");
                }
            }
            sb.append("]");
            return sb.toString();
        } else if (expr instanceof FunctionCallExpr) {
            FunctionCallExpr callExpr = (FunctionCallExpr) expr;
            return "FunctionCall: " + callExpr.functionName + "(" + printArguments(callExpr.arguments) + ")";
        }

        return "UnknownExpr";
    }

    private static String printArguments(List<Expr> arguments) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arguments.size(); i++) {
            sb.append(printExpr(arguments.get(i)));
            if (i < arguments.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }
}
