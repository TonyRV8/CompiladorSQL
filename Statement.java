import java.util.List;

// Clase base para todas las declaraciones SQL
public abstract class Statement {
    public abstract void execute();
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
    public void execute() {
        // Lógica para ejecutar la declaración SELECT
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
    public void execute() {
        // Lógica para manejar las tablas en el FROM
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
    public void execute() {
        // Lógica para manejar la condición WHERE
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
    public void execute() {
        // Implementación específica para ejecutar la consulta si es necesario
    }

    @Override
    public String toString() {
        return "QueryStatement { " +
               "select=" + select +
               ", from=" + from +
               ", where=" + (where != null ? where : "null") +
               " }";
    }
}
