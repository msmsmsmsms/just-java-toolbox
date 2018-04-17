package de.justsoftware.toolbox.stream;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.ImmutableTable;

/**
 * Collectors for streams of {@link java.util.Map.Entry}s. Handy usable with {@link EntryStream}.
 */
@ParametersAreNonnullByDefault
public class EntryCollectors {

    @Nonnull
    public static <K, V> Collector<Map.Entry<K, V>, ?, Map<K, V>> toMap() {
        return Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue);
    }

    @Nonnull
    public static <K, V> Collector<Map.Entry<K, V>, ?, ImmutableMap<K, V>> toImmutableMap() {
        return ImmutableMap.toImmutableMap(Map.Entry::getKey, Map.Entry::getValue);
    }

    @Nonnull
    public static <K, V> Collector<Map.Entry<K, V>, ?, ImmutableSetMultimap<K, V>> toImmutableSetMultimap() {
        return ImmutableSetMultimap.toImmutableSetMultimap(Map.Entry::getKey, Map.Entry::getValue);
    }

    @Nonnull
    public static <K, V> Collector<Map.Entry<K, V>, ?, ImmutableListMultimap<K, V>> toImmutableListMultimap() {
        return ImmutableListMultimap.toImmutableListMultimap(Map.Entry::getKey, Map.Entry::getValue);
    }

    @Nonnull
    public static <R, C, V> Collector<Map.Entry<R, C>, ?, ImmutableTable<R, C, V>> toImmutableTable(
            final BiFunction<R, C, V> valueFunction) {
        return ImmutableTable.toImmutableTable(Map.Entry::getKey, Map.Entry::getValue,
                e -> valueFunction.apply(e.getKey(), e.getValue()));
    }

}
