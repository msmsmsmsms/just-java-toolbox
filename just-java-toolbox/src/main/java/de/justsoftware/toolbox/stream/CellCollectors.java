package de.justsoftware.toolbox.stream;

import java.util.stream.Collector;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;

public class CellCollectors {

    @Nonnull
    public static <R, C, V> Collector<Table.Cell<R, C, V>, ?, ImmutableTable<R, C, V>> toImmutableTable() {
        return ImmutableTable.toImmutableTable(Table.Cell::getRowKey, Table.Cell::getColumnKey, Table.Cell::getValue);
    }

    @Nonnull
    public static <R, C> Collector<Table.Cell<R, C, ?>, ?, ImmutableSetMultimap<R, C>> toKeysMultimap() {
        return ImmutableSetMultimap.toImmutableSetMultimap(Table.Cell::getRowKey, Table.Cell::getColumnKey);
    }

}
