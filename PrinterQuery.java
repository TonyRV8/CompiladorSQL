public class PrinterQuery {

    public static void print(Statement stmt) {
        if (stmt instanceof SelectStatement) {
            printSelect((SelectStatement) stmt);
        } else if (stmt instanceof FromStatement) {
            printFrom((FromStatement) stmt);
        } else if (stmt instanceof WhereStatement) {
            printWhere((WhereStatement) stmt);
        } else if (stmt instanceof QueryStatement) {
            printQuery((QueryStatement) stmt);
        } else {
            System.out.println("Unsupported statement type.");
        }
    }

    private static void printSelect(SelectStatement stmt) {
        System.out.println("SELECT:");
        System.out.println("Columns: " + stmt.columns);
    }

    private static void printFrom(FromStatement stmt) {
        System.out.println("FROM:");
        System.out.println("Tables: " + stmt.tables);
    }

    private static void printWhere(WhereStatement stmt) {
        System.out.println("WHERE:");
        System.out.println("Condition: " + stmt.condition);
    }

    private static void printQuery(QueryStatement stmt) {
        System.out.println("QUERY:");
        print(stmt.select);
        print(stmt.from);
        if (stmt.where != null) {
            print(stmt.where);
        }
    }
}
