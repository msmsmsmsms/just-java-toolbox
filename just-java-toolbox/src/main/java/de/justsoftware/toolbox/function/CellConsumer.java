package de.justsoftware.toolbox.function;

import java.util.function.Consumer;

import com.google.common.collect.Table;

@FunctionalInterface
public interface CellConsumer<R, C, V> extends Consumer<Table.Cell<? extends R, ? extends C, ? extends V>> {

    void accept(R row, C column, V value);

    @Override
    default void accept(final Table.Cell<? extends R, ? extends C, ? extends V> cell) {
        accept(cell.getRowKey(), cell.getColumnKey(), cell.getValue());
    }

}
