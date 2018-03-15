package de.justsoftware.toolbox.mybatis.result;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collector;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.ImmutableTable;

/**
 * fist step for partition selects, used to define the indexing for (multi)maps or collecting lists
 */
@ParametersAreNonnullByDefault
public final class ResultHandlerBuilder<ID> {

    public static final String KEY = "key";
    public static final String ROW = "row";
    public static final String COL = "col";
    public static final String VALUE = "value";

    private final Set<? extends ID> _ids;
    private final InternalDAOSupport _daoSupport;

    public ResultHandlerBuilder(final InternalDAOSupport daoSupport, final Set<? extends ID> ids) {
        _daoSupport = daoSupport;
        _ids = ids;
    }

    /**
     * return a function which extracts a property from a map
     * 
     * @param property
     *            name of the field
     */
    public static <T> Function<Map<String, Object>, T> property(final String property) {
        return input -> input != null
            ? (T) input.get(property)
            : null;
    }

    /**
     * index the result by the result of a function
     */
    @Nonnull
    public <K, V> MapResultHandlerBuilder<ID, K, V, V> index(final Function<? super V, K> keyFunction) {
        return new MapResultHandlerBuilder<>(this, keyFunction, Function.identity());
    }

    /**
     * extract key and value like properties out of a map to create a new (multi-)map.
     */
    @Nonnull
    public <K, V> MapResultHandlerBuilder<ID, K, V, Map<String, Object>> projection(final String key, final String value) {
        return new MapResultHandlerBuilder<>(this, property(key), property(value));
    }

    /**
     * extract property {@link ResultHandlerBuilder#KEY} and {@link ResultHandlerBuilder#VALUE} out of a map and use them to
     * index a new map
     */
    @Nonnull
    public <K, V> MapResultHandlerBuilder<ID, K, V, Map<String, Object>> projection() {
        return projection(KEY, VALUE);
    }

    /**
     * shortcut for projection().asMap().query(query) but without the need of supplying the type
     */
    @Nonnull
    public <K, V> ImmutableMap<K, V> asMap(final Query<ID, Map<String, Object>> query) {
        return this.<K, V>projection().asMap().query(query);
    }

    /**
     * shortcut for projection().asMap().query(query) but without the need of supplying the type
     */
    @Nonnull
    public <K, V> ImmutableSetMultimap<K, V> asSetMultimap(final Query<ID, Map<String, Object>> query) {
        return this.<K, V>projection().asSetMultimap().query(query);
    }

    /**
     * shortcut for projection().asListMultimap().query(query) but without the need of supplying the type
     */
    @Nonnull
    public <K, V> ImmutableListMultimap<K, V> asListMultimap(final Query<ID, Map<String, Object>> query) {
        return this.<K, V>projection().asListMultimap().query(query);
    }

    /**
     * extract row, column and value like properties out of a map to create a new Table.
     */
    @Nonnull
    public <R, C, V> CollectorResultHandler<ID, Map<String, Object>, ?, ImmutableTable<R, C, V>> projectionTable(
            final String row, final String col, final String value) {
        return collect(ImmutableTable.toImmutableTable(property(row), property(col), property(value)));
    }

    /**
     * Extract property {@link ResultHandlerBuilder#ROW}, {@link ResultHandlerBuilder#COL} and
     * {@link ResultHandlerBuilder#COL} out of a map and use them to index a new table.
     */
    @Nonnull
    public <R, C, V> CollectorResultHandler<ID, Map<String, Object>, ?, ImmutableTable<R, C, V>> projectionTable() {
        return projectionTable(ROW, COL, VALUE);
    }

    /**
     * shortcut for projectionTable().query(query) but without the need of supplying the type
     */
    @Nonnull
    public <R, C, V> ImmutableTable<R, C, V> asTable(final Query<ID, Map<String, Object>> query) {
        return this.<R, C, V>projectionTable().query(query);
    }

    /**
     * collect the results with a {@link Collector}
     */
    @Nonnull
    public <T, R> CollectorResultHandler<ID, T, ?, R> collect(final Collector<T, ?, R> collector) {
        return new CollectorResultHandler<>(this, collector);
    }

    /**
     * shortcut for collect(collector).query(query) but without the need of supplying the type
     */
    @Nonnull
    public <DB_RESULT, R> R collect(final Collector<DB_RESULT, ?, R> collector, final Query<ID, DB_RESULT> query) {
        return collect(collector).query(query);
    }

    /**
     * put the results into an {@link com.google.common.collect.ImmutableList}
     */
    @Nonnull
    public <R> CollectorResultHandler<ID, ?, ?, ImmutableList<R>> asList() {
        return collect(ImmutableList.toImmutableList());
    }

    /**
     * shortcut for asList().query(query) but without the need of supplying the type
     */
    @Nonnull
    public <DB_RESULT> ImmutableList<DB_RESULT> asList(final Query<ID, DB_RESULT> query) {
        return collect(ImmutableList.toImmutableList(), query);
    }

    /**
     * put the results into an {@link com.google.common.collect.ImmutableSet}
     */
    @Nonnull
    public <R> CollectorResultHandler<ID, ?, ?, ImmutableSet<R>> asSet() {
        return collect(ImmutableSet.toImmutableSet());
    }

    /**
     * shortcut for asSet().query(query) but without the need of supplying the type
     */
    @Nonnull
    public <DB_RESULT> ImmutableSet<DB_RESULT> asSet(final Query<ID, DB_RESULT> query) {
        return collect(ImmutableSet.toImmutableSet(), query);
    }

    /**
     * execute a query for each partition
     * <p>
     * WARNING! If you are modifying the database, you have to take care of transaction management!
     * <p>
     * Without a transaction each operation will be executed in its own transaction.
     */
    public void noResult(final NoResultQuery<ID> query) {
        _daoSupport.partition(_ids, query);
    }

}
