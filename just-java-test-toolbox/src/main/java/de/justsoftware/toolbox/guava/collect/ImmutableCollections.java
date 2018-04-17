package de.justsoftware.toolbox.guava.collect;

import java.util.Iterator;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.google.common.base.Functions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Streams;

/**
 * Utilities to make conversion to immutable collections in tests less verbose
 * when using them as static imports.
 */
@ParametersAreNonnullByDefault
public final class ImmutableCollections {

    private ImmutableCollections() {
        // utility class
    }

    /**
     * Returns a {@link Stream} which contains the given elements
     */
    @SafeVarargs
    @Nonnull
    public static <T> Stream<T> stream(final T... items) {
        return Stream.of(items);
    }

    /**
     * Returns a {@link Stream} which contains the elements of the given iterable
     */
    @Nonnull
    public static <T> Stream<T> stream(final Iterable<T> iterable) {
        return Streams.stream(iterable);
    }

    /**
     * Returns a {@link Stream} which contains the elements of the given iterator
     */
    @Nonnull
    public static <T> Stream<T> stream(final Iterator<T> iterator) {
        return Streams.stream(iterator);
    }

    /**
     * Returns an {@link ImmutableSet} containing the given items.
     */
    @SafeVarargs
    @Nonnull
    public static <T> ImmutableSet<T> set(final T... items) {
        return ImmutableSet.copyOf(items);
    }

    /**
     * Returns an {@link ImmutableSet} from the given iterable.
     */
    @Nonnull
    public static <T> ImmutableSet<T> set(final Iterable<T> iterable) {
        return ImmutableSet.copyOf(iterable);
    }

    /**
     * Returns an {@link ImmutableSet} from the given iterator.
     */
    @Nonnull
    public static <T> ImmutableSet<T> set(final Iterator<T> iterator) {
        return ImmutableSet.copyOf(iterator);
    }

    /**
     * Returns an {@link ImmutableSet} from the given stream.
     */
    @Nonnull
    public static <T> ImmutableSet<T> set(final Stream<T> stream) {
        return stream.collect(ImmutableSet.toImmutableSet());
    }

    /**
     * Returns an {@link ImmutableList} containing the given items.
     */
    @SafeVarargs
    @Nonnull
    public static <T> ImmutableList<T> list(final T... items) {
        return ImmutableList.copyOf(items);
    }

    /**
     * Returns an {@link ImmutableList} from the given iterable.
     */
    @Nonnull
    public static <T> ImmutableList<T> list(final Iterable<T> iterable) {
        return ImmutableList.copyOf(iterable);
    }

    /**
     * Returns an {@link ImmutableList} from the given iterator.
     */
    @Nonnull
    public static <T> ImmutableList<T> list(final Iterator<T> iterator) {
        return ImmutableList.copyOf(iterator);
    }

    /**
     * Returns an {@link ImmutableList} from the given stream.
     */
    @Nonnull
    public static <T> ImmutableList<T> list(final Stream<T> stream) {
        return stream.collect(ImmutableList.toImmutableList());
    }

    /**
     * Returns an empty {@link ImmutableMap}.
     */
    @Nonnull
    public static <K, V> ImmutableMap<K, V> map() {
        return ImmutableMap.of();
    }

    /**
     * Returns an {@link ImmutableMap} with the given key value pair.
     */
    @Nonnull
    public static <K, V> ImmutableMap<K, V> map(final K key, final V value) {
        return ImmutableMap.of(key, value);
    }

    /**
     * Returns an {@link ImmutableMap} with the given key value pairs.
     */
    @Nonnull
    public static <K, V> ImmutableMap<K, V> map(
            final K k1, final V v1,
            final K k2, final V v2) {
        return ImmutableMap.of(k1, v1, k2, v2);
    }

    /**
     * Returns an {@link ImmutableMap} with the given key value pairs.
     */
    @Nonnull
    public static <K, V> ImmutableMap<K, V> map(
            final K k1, final V v1,
            final K k2, final V v2,
            final K k3, final V v3) {
        return ImmutableMap.of(k1, v1, k2, v2, k3, v3);
    }

    /**
     * Returns an {@link ImmutableMap} with the given key value pairs.
     */
    @Nonnull
    public static <K, V> ImmutableMap<K, V> map(
            final K k1, final V v1,
            final K k2, final V v2,
            final K k3, final V v3,
            final K k4, final V v4) {
        return ImmutableMap.of(k1, v1, k2, v2, k3, v3, k4, v4);
    }

    /**
     * Returns an {@link ImmutableMap} with the given key value pairs.
     */
    @Nonnull
    public static <K, V> ImmutableMap<K, V> map(
            final K k1, final V v1,
            final K k2, final V v2,
            final K k3, final V v3,
            final K k4, final V v4,
            final K k5, final V v5) {
        return ImmutableMap.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5);
    }

    /**
     * Returns an empty {@link ImmutableSetMultimap}.
     */
    @Nonnull
    public static <K, V> ImmutableSetMultimap<K, V> setMultimap() {
        return ImmutableSetMultimap.of();
    }

    /**
     * Returns an {@link ImmutableSetMultimap} with the given key value pair.
     */
    @Nonnull
    public static <K, V> ImmutableSetMultimap<K, V> setMultimap(final K key, final V value) {
        return ImmutableSetMultimap.of(key, value);
    }

    /**
     * Returns an {@link ImmutableSetMultimap} with the given key value pairs.
     */
    @Nonnull
    public static <K, V> ImmutableSetMultimap<K, V> setMultimap(
            final K k1, final V v1,
            final K k2, final V v2) {
        return ImmutableSetMultimap.of(k1, v1, k2, v2);
    }

    /**
     * Returns an {@link ImmutableSetMultimap} with the given key value pairs.
     */
    @Nonnull
    public static <K, V> ImmutableSetMultimap<K, V> setMultimap(
            final K k1, final V v1,
            final K k2, final V v2,
            final K k3, final V v3) {
        return ImmutableSetMultimap.of(k1, v1, k2, v2, k3, v3);
    }

    /**
     * Returns an {@link ImmutableSetMultimap} with the given key value pairs.
     */
    @Nonnull
    public static <K, V> ImmutableSetMultimap<K, V> setMultimap(
            final K k1, final V v1,
            final K k2, final V v2,
            final K k3, final V v3,
            final K k4, final V v4) {
        return ImmutableSetMultimap.of(k1, v1, k2, v2, k3, v3, k4, v4);
    }

    /**
     * Returns an {@link ImmutableSetMultimap} with the given key value pairs.
     */
    @Nonnull
    public static <K, V> ImmutableSetMultimap<K, V> setMultimap(
            final K k1, final V v1,
            final K k2, final V v2,
            final K k3, final V v3,
            final K k4, final V v4,
            final K k5, final V v5) {
        return ImmutableSetMultimap.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5);
    }

    /**
     * Returns an {@link ImmutableSetMultimap} which contains the given items for one given key.
     */
    @SafeVarargs
    @Nonnull
    public static <K, V> ImmutableSetMultimap<K, V> setMultimapOneKey(final K key, final V... items) {
        return setMultimapOneKey(key, stream(items));
    }

    /**
     * Returns an {@link ImmutableSetMultimap} which contains the given iterable for one given key.
     */
    @Nonnull
    public static <K, V> ImmutableSetMultimap<K, V> setMultimapOneKey(final K key, final Iterable<V> iterable) {
        return setMultimapOneKey(key, stream(iterable));
    }

    /**
     * Returns an {@link ImmutableSetMultimap} which contains the given iterable for one given key.
     */
    @Nonnull
    public static <K, V> ImmutableSetMultimap<K, V> setMultimapOneKey(final K key, final Iterator<V> iterator) {
        return setMultimapOneKey(key, stream(iterator));
    }

    /**
     * Returns an {@link ImmutableSetMultimap} which contains the given iterable for one given key.
     */
    @Nonnull
    public static <K, V> ImmutableSetMultimap<K, V> setMultimapOneKey(final K key, final Stream<V> stream) {
        return stream.collect(ImmutableSetMultimap.toImmutableSetMultimap(Functions.constant(key), Function.identity()));
    }

    /**
     * Returns an empty {@link ImmutableListMultimap}.
     */
    @Nonnull
    public static <K, V> ImmutableListMultimap<K, V> listMultimap() {
        return ImmutableListMultimap.of();
    }

    /**
     * Returns an {@link ImmutableListMultimap} with the given key value pair.
     */
    @Nonnull
    public static <K, V> ImmutableListMultimap<K, V> listMultimap(final K key, final V value) {
        return ImmutableListMultimap.of(key, value);
    }

    /**
     * Returns an {@link ImmutableListMultimap} with the given key value pairs.
     */
    @Nonnull
    public static <K, V> ImmutableListMultimap<K, V> listMultimap(
            final K k1, final V v1,
            final K k2, final V v2) {
        return ImmutableListMultimap.of(k1, v1, k2, v2);
    }

    /**
     * Returns an {@link ImmutableListMultimap} with the given key value pairs.
     */
    @Nonnull
    public static <K, V> ImmutableListMultimap<K, V> listMultimap(
            final K k1, final V v1,
            final K k2, final V v2,
            final K k3, final V v3) {
        return ImmutableListMultimap.of(k1, v1, k2, v2, k3, v3);
    }

    /**
     * Returns an {@link ImmutableListMultimap} with the given key value pairs.
     */
    @Nonnull
    public static <K, V> ImmutableListMultimap<K, V> listMultimap(
            final K k1, final V v1,
            final K k2, final V v2,
            final K k3, final V v3,
            final K k4, final V v4) {
        return ImmutableListMultimap.of(k1, v1, k2, v2, k3, v3, k4, v4);
    }

    /**
     * Returns an {@link ImmutableListMultimap} with the given key value pairs.
     */
    @Nonnull
    public static <K, V> ImmutableListMultimap<K, V> listMultimap(
            final K k1, final V v1,
            final K k2, final V v2,
            final K k3, final V v3,
            final K k4, final V v4,
            final K k5, final V v5) {
        return ImmutableListMultimap.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5);
    }

    /**
     * Returns an {@link ImmutableListMultimap} which contains the given items for one given key.
     */
    @SafeVarargs
    @Nonnull
    public static <K, V> ImmutableListMultimap<K, V> listMultimapOneKey(final K key, final V... items) {
        return listMultimapOneKey(key, stream(items));
    }

    /**
     * Returns an {@link ImmutableListMultimap} which contains the given iterable for one given key.
     */
    @Nonnull
    public static <K, V> ImmutableListMultimap<K, V> listMultimapOneKey(final K key, final Iterable<V> iterable) {
        return listMultimapOneKey(key, stream(iterable));
    }

    /**
     * Returns an {@link ImmutableListMultimap} which contains the given iterator for one given key.
     */
    @Nonnull
    public static <K, V> ImmutableListMultimap<K, V> listMultimapOneKey(final K key, final Iterator<V> iterator) {
        return listMultimapOneKey(key, stream(iterator));
    }

    /**
     * Returns an {@link ImmutableListMultimap} which contains the given stream for one given key.
     */
    @Nonnull
    public static <K, V> ImmutableListMultimap<K, V> listMultimapOneKey(final K key, final Stream<V> stream) {
        return stream.collect(ImmutableListMultimap.toImmutableListMultimap(Functions.constant(key), Function.identity()));
    }

    /**
     * Returns an {@link ImmutableTable} with the given row key and value tuple.
     */
    @Nonnull
    public static <R, C, V> ImmutableTable<R, C, V> table(final R r, final C c, final V v) {
        return ImmutableTable.of(r, c, v);
    }

    /**
     * Returns an {@link ImmutableTable} with the given row key and value tuples.
     */
    @Nonnull
    public static <R, C, V> ImmutableTable<R, C, V> table(
            final R r1, final C c1, final V v1,
            final R r2, final C c2, final V v2) {
        return ImmutableTable.<R, C, V>builder()
                .put(r1, c1, v1)
                .put(r2, c2, v2)
                .build();
    }

    /**
     * Returns an {@link ImmutableTable} with the given row key and value tuples.
     */
    @Nonnull
    public static <R, C, V> ImmutableTable<R, C, V> table(
            final R r1, final C c1, final V v1,
            final R r2, final C c2, final V v2,
            final R r3, final C c3, final V v3) {
        return ImmutableTable.<R, C, V>builder()
                .put(r1, c1, v1)
                .put(r2, c2, v2)
                .put(r3, c3, v3)
                .build();
    }

    /**
     * Returns an {@link ImmutableTable} with the given row key and value tuples.
     */
    @Nonnull
    public static <R, C, V> ImmutableTable<R, C, V> table(
            final R r1, final C c1, final V v1,
            final R r2, final C c2, final V v2,
            final R r3, final C c3, final V v3,
            final R r4, final C c4, final V v4) {
        return ImmutableTable.<R, C, V>builder()
                .put(r1, c1, v1)
                .put(r2, c2, v2)
                .put(r3, c3, v3)
                .put(r4, c4, v4)
                .build();
    }

    /**
     * Returns an {@link ImmutableTable} with the given row key and value tuples.
     */
    @Nonnull
    public static <R, C, V> ImmutableTable<R, C, V> table(
            final R r1, final C c1, final V v1,
            final R r2, final C c2, final V v2,
            final R r3, final C c3, final V v3,
            final R r4, final C c4, final V v4,
            final R r5, final C c5, final V v5) {
        return ImmutableTable.<R, C, V>builder()
                .put(r1, c1, v1)
                .put(r2, c2, v2)
                .put(r3, c3, v3)
                .put(r4, c4, v4)
                .put(r5, c5, v5)
                .build();
    }

}
