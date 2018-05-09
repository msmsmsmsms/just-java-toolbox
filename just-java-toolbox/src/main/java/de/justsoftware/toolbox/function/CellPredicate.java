package de.justsoftware.toolbox.function;

import java.util.function.Predicate;

import com.google.common.collect.Table;

@FunctionalInterface
public interface CellPredicate<R, C, V> extends Predicate<Table.Cell<? extends R, ? extends C, ? extends V>> {

    boolean test(R row, C column, V value);

    @Override
    default boolean test(final Table.Cell<? extends R, ? extends C, ? extends V> cell) {
        return test(cell.getRowKey(), cell.getColumnKey(), cell.getValue());
    }

}
