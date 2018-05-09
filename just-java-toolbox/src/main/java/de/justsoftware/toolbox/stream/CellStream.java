package de.justsoftware.toolbox.stream;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import com.google.common.collect.Table;
import com.google.common.collect.Tables;

import de.justsoftware.toolbox.function.CellConsumer;
import de.justsoftware.toolbox.function.CellFunction;
import de.justsoftware.toolbox.function.CellPredicate;

/**
 * A {@link Stream} for {@link Table.Cell}s
 *
 * @param <R>
 *            type of the rows of the cells in the stream
 * @param <C>
 *            type of the columns of the cells in the stream
 * @param <V>
 *            type of the values of the cells in the stream
 */
public class CellStream<R, C, V> {

    private final Stream<Table.Cell<R, C, V>> _stream;

    private CellStream(final Stream<Table.Cell<R, C, V>> stream) {
        _stream = stream;
    }

    @Nonnull
    public static <R, C, V> CellStream<R, C, V> from(final Table<R, C, V> table) {
        return from(table.cellSet().stream());
    }

    @Nonnull
    public static <R, C, V> CellStream<R, C, V> from(final Stream<Table.Cell<R, C, V>> s) {
        return new CellStream<>(s);
    }

    @Nonnull
    public static <S, R, C, V> CellStream<R, C, V> from(final Stream<S> s,
            final Function<? super S, ? extends R> rowFunction,
            final Function<? super S, ? extends C> colFunction,
            final Function<? super S, ? extends V> valueFunction) {
        return from(s.map(k -> Tables.immutableCell(
                rowFunction.apply(k),
                colFunction.apply(k),
                valueFunction.apply(k))));
    }

    @Nonnull
    public static <R, C, V> CellStream<R, C, V> from(final EntryStream<R, C> s,
            final BiFunction<? super R, ? super C, ? extends V> f) {
        return from(s.map((r, c) -> Tables.immutableCell(r, c, f.apply(r, c))));
    }

    @Nonnull
    public Stream<Table.Cell<R, C, V>> stream() {
        return _stream;
    }

    @Nonnull
    public CellStream<R, C, V> distinct() {
        return from(_stream.distinct());
    }

    @Nonnull
    public CellStream<R, C, V> peek(final CellConsumer<? super R, ? super C, ? super V> action) {
        return from(_stream.peek(action));
    }

    @Nonnull
    public CellStream<R, C, V> skip(final long n) {
        return from(_stream.skip(n));
    }

    @Nonnull
    public CellStream<R, C, V> limit(final long maxSize) {
        return from(_stream.limit(maxSize));
    }

    @Nonnull
    public CellStream<R, C, V> filterRow(final Predicate<? super R> rowPredicate) {
        return from(_stream.filter(e -> rowPredicate.test(e.getRowKey())));
    }

    @Nonnull
    public CellStream<R, C, V> filterColumn(final Predicate<? super C> columnPredicate) {
        return from(_stream.filter(e -> columnPredicate.test(e.getColumnKey())));
    }

    @Nonnull
    public CellStream<R, C, V> filterValue(final Predicate<? super V> valuePredicate) {
        return from(_stream.filter(e -> valuePredicate.test(e.getValue())));
    }

    @Nonnull
    public CellStream<R, C, V> filter(final CellPredicate<? super R, ? super C, ? super V> predicate) {
        return from(_stream.filter(predicate));
    }

    @Nonnull
    public <T> CellStream<T, C, V> mapRow(final Function<? super R, ? extends T> rowMapper) {
        return from(
                _stream.map(e -> Tables.immutableCell(rowMapper.apply(e.getRowKey()), e.getColumnKey(), e.getValue())));
    }

    @Nonnull
    public <T> CellStream<R, T, V> mapCol(final Function<? super C, ? extends T> colMapper) {
        return from(
                _stream.map(e -> Tables.immutableCell(e.getRowKey(), colMapper.apply(e.getColumnKey()), e.getValue())));
    }

    @Nonnull
    public <T> CellStream<R, C, T> mapValue(final Function<? super V, ? extends T> valueMapper) {
        return from(
                _stream.map(e -> Tables.immutableCell(e.getRowKey(), e.getColumnKey(), valueMapper.apply(e.getValue()))));
    }

    @Nonnull
    public <T> Stream<T> map(final CellFunction<? super R, ? super C, ? super V, ? extends T> mapper) {
        return _stream.map(mapper);
    }

    @Nonnull
    public <RR, RC, RV> CellStream<RR, RC, RV> flatMap(
            final CellFunction<? super R, ? super C, ? super V, ? extends CellStream<RR, RC, RV>> mapper) {
        return from(_stream.map(mapper).flatMap(CellStream::stream));
    }

    @Nonnull
    public <T> Stream<T> flatMapToObj(final CellFunction<? super R, ? super C, ? super V, ? extends Stream<T>> mapper) {
        return _stream.flatMap(mapper);
    }

    @Nonnull
    public CellStream<R, C, V> sortedByRow(final Comparator<? super R> comparator) {
        return from(_stream.sorted(Comparator.comparing(Table.Cell::getRowKey, comparator)));
    }

    @Nonnull
    public CellStream<R, C, V> sortedByColumn(final Comparator<? super C> comparator) {
        return from(_stream.sorted(Comparator.comparing(Table.Cell::getColumnKey, comparator)));
    }

    @Nonnull
    public CellStream<R, C, V> sortedByValue(final Comparator<? super V> comparator) {
        return from(_stream.sorted(Comparator.comparing(Table.Cell::getValue, comparator)));
    }

    public boolean allMatch(final CellPredicate<? super R, ? super C, ? super V> predicate) {
        return _stream.allMatch(predicate);
    }

    public boolean anyMatch(final CellPredicate<? super R, ? super C, ? super V> predicate) {
        return _stream.anyMatch(predicate);
    }

    public boolean noneMatch(final CellPredicate<? super R, ? super C, ? super V> predicate) {
        return _stream.noneMatch(predicate);
    }

    public long count() {
        return _stream.count();
    }

    @Nonnull
    public Stream<R> rows() {
        return _stream.map(Table.Cell::getRowKey);
    }

    @Nonnull
    public Stream<C> columns() {
        return _stream.map(Table.Cell::getColumnKey);
    }

    @Nonnull
    public Stream<V> values() {
        return _stream.map(Table.Cell::getValue);
    }

    @Nonnull
    public Optional<Table.Cell<R, C, V>> maxByRow(final Comparator<? super R> comparator) {
        return _stream.max(Comparator.comparing(Table.Cell::getRowKey, comparator));
    }

    @Nonnull
    public Optional<Table.Cell<R, C, V>> maxByColumn(final Comparator<? super C> comparator) {
        return _stream.max(Comparator.comparing(Table.Cell::getColumnKey, comparator));
    }

    @Nonnull
    public Optional<Table.Cell<R, C, V>> maxByValue(final Comparator<? super V> comparator) {
        return _stream.max(Comparator.comparing(Table.Cell::getValue, comparator));
    }

    @Nonnull
    public Optional<Table.Cell<R, C, V>> minByRow(final Comparator<? super R> comparator) {
        return _stream.min(Comparator.comparing(Table.Cell::getRowKey, comparator));
    }

    @Nonnull
    public Optional<Table.Cell<R, C, V>> minByColumn(final Comparator<? super C> comparator) {
        return _stream.min(Comparator.comparing(Table.Cell::getColumnKey, comparator));
    }

    @Nonnull
    public Optional<Table.Cell<R, C, V>> minByValue(final Comparator<? super V> comparator) {
        return _stream.min(Comparator.comparing(Table.Cell::getValue, comparator));
    }

    public void forEach(final CellConsumer<? super R, ? super C, ? super V> action) {
        _stream.forEach(action);
    }

    public void forEachOrdered(final CellConsumer<? super R, ? super C, ? super V> action) {
        _stream.forEachOrdered(action);
    }

    public <T> T collect(final Collector<? super Table.Cell<R, C, V>, ?, T> collector) {
        return _stream.collect(collector);
    }

    @Nonnull
    public static <R, C, V> CellStream<R, C, V> of() {
        return from(Stream.of());
    }

    @Nonnull
    public static <R, C, V> CellStream<R, C, V> of(
            final R r1, final C c1, final V v1) {
        return from(Stream.of(
                Tables.immutableCell(r1, c1, v1)));
    }

    @Nonnull
    public static <R, C, V> CellStream<R, C, V> of(
            final R r1, final C c1, final V v1,
            final R r2, final C c2, final V v2) {
        return from(Stream.of(
                Tables.immutableCell(r1, c1, v1),
                Tables.immutableCell(r2, c2, v2)));
    }

    @Nonnull
    public static <R, C, V> CellStream<R, C, V> of(
            final R r1, final C c1, final V v1,
            final R r2, final C c2, final V v2,
            final R r3, final C c3, final V v3) {
        return from(Stream.of(
                Tables.immutableCell(r1, c1, v1),
                Tables.immutableCell(r2, c2, v2),
                Tables.immutableCell(r3, c3, v3)));
    }

    @Nonnull
    public static <R, C, V> CellStream<R, C, V> of(
            final R r1, final C c1, final V v1,
            final R r2, final C c2, final V v2,
            final R r3, final C c3, final V v3,
            final R r4, final C c4, final V v4) {
        return from(Stream.of(
                Tables.immutableCell(r1, c1, v1),
                Tables.immutableCell(r2, c2, v2),
                Tables.immutableCell(r3, c3, v3),
                Tables.immutableCell(r4, c4, v4)));
    }

}
