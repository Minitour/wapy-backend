package io.nbrs.database.sql;

import io.nbrs.database.exception.UnimplementedException;
import io.nbrs.database.sql.clause.Clause;

/**
 * Created by Antonio Zaitoun on 23/02/2018.
 */
public class Where implements Clause {
    public final String syntax;
    public final Object[] values;

    public Where(String syntax, Object...values) {
        this.syntax = syntax;
        this.values = values;
    }

    @Override
    public String dump() {
        throw new UnimplementedException();
    }
}
