import java.util.List;

public abstract class Statement {
    public abstract void execute();
}

class SelectStatement extends Statement {
    public final List<Expr> columns;
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
        System.out.println("Executing SELECT statement...");
    }

    @Override
    public String toString() {
        return (isDistinct ? "DISTINCT " : "") + "SELECT " + columns.toString();
    }
}

class FromStatement extends Statement {
    public final List<String> tables;

    public FromStatement(List<String> tables) {
        this.tables = tables;
    }

    @Override
    public void execute() {
        System.out.println("Executing FROM statement...");
    }

    @Override
    public String toString() {
        return "FROM " + tables;
    }
}

class WhereStatement extends Statement {
    public final Expr condition;

    public WhereStatement(Expr condition) {
        this.condition = condition;
    }

    @Override
    public void execute() {
        System.out.println("Executing WHERE statement...");
    }

    @Override
    public String toString() {
        return "WHERE " + condition;
    }
}

class QueryStatement extends Statement {
    public final SelectStatement select;
    public final FromStatement from;
    public final WhereStatement where;

    public QueryStatement(SelectStatement select, FromStatement from, WhereStatement where) {
        this.select = select;
        this.from = from;
        this.where = where;
    }

    @Override
    public void execute() {
        select.execute();
        from.execute();
        if (where != null) {
            where.execute();
        }
    }

    @Override
    public String toString() {
        return select + "\n" + from + (where != null ? "\n" + where : "");
    }
}
