package de.justsoftware.toolbox.mybatis.result;

import java.util.function.Function;
import java.util.stream.Collector;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSetMultimap;

/**
 * ResultHandlerBuilder for Map like data structures.
 */
@ParametersAreNonnullByDefault
public final class MapResultHandlerBuilder<ID, K, V, DB_RESULT> {

    final Function<? super DB_RESULT, K> _keyFunction;
    final Function<? super DB_RESULT, V> _valueFunction;
    final ResultHandlerBuilder<ID> _resultHandlerBuilder;

    MapResultHandlerBuilder(
            final ResultHandlerBuilder<ID> resultHandlerBuilder,
            final Function<? super DB_RESULT, K> keyFunction,
            final Function<? super DB_RESULT, V> valueFunction) {
        _resultHandlerBuilder = resultHandlerBuilder;
        _keyFunction = keyFunction;
        _valueFunction = valueFunction;
    }

    @Nonnull
    private <R> CollectorResultHandler<ID, DB_RESULT, ?, R> collect(final Collector<DB_RESULT, ?, R> collector) {
        return new CollectorResultHandler<>(_resultHandlerBuilder, collector);
    }

    /**
     * the query will return an {@link com.google.common.collect.ImmutableMap}
     */
    @Nonnull
    public CollectorResultHandler<ID, DB_RESULT, ?, ImmutableMap<K, V>> asMap() {
        return collect(ImmutableMap.toImmutableMap(_keyFunction, _valueFunction));
    }

    /**
     * the query will return an {@link com.google.common.collect.ImmutableBiMap}
     */
    @Nonnull
    public CollectorResultHandler<ID, DB_RESULT, ?, ImmutableBiMap<K, V>> asBiMap() {
        return collect(ImmutableBiMap.toImmutableBiMap(_keyFunction, _valueFunction));
    }

    /**
     * the query will return an {@link com.google.common.collect.ImmutableSetMultimap}
     */
    @Nonnull
    public CollectorResultHandler<ID, DB_RESULT, ?, ImmutableSetMultimap<K, V>> asSetMultimap() {
        return collect(ImmutableSetMultimap.toImmutableSetMultimap(_keyFunction, _valueFunction));
    }

    /**
     * the query will return an {@link com.google.common.collect.ImmutableListMultimap}
     */
    @Nonnull
    public CollectorResultHandler<ID, DB_RESULT, ?, ImmutableListMultimap<K, V>> asListMultimap() {
        return collect(ImmutableListMultimap.toImmutableListMultimap(_keyFunction, _valueFunction));
    }

}
