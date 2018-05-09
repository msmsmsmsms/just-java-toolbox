package de.justsoftware.toolbox.function;

import java.util.function.Function;

import com.google.common.collect.Table;

@FunctionalInterface
public interface CellFunction<R, C, V, T> extends Function<Table.Cell<? extends R, ? extends C, ? extends V>, T> {

    T apply(R row, C column, V value);

    @Override
    default T apply(final Table.Cell<? extends R, ? extends C, ? extends V> cell) {
        return apply(cell.getRowKey(), cell.getColumnKey(), cell.getValue());
    }

}
