package de.justsoftware.toolbox.guava.collect;

import java.util.Iterator;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Streams;

/**
 * methods which are missing in {@link com.google.common.collect.Multimaps}
 * 
 * @author Jan Burkhardt (jan.burkhardt@just.social)
 */
@ParametersAreNonnullByDefault
public final class Multimaps2 {

    private Multimaps2() {
        super();
    }

    /**
     * same as {@link com.google.common.collect.Multimaps#index} but returning a
     * {@link ImmutableSetMultimap}.
     * 
     * @deprecated consider using the stream api directly
     */
    @Nonnull
    @Deprecated
    public static <K, V> ImmutableSetMultimap<K, V> indexSetMultimap(final Iterable<? extends V> values,
            final Function<? super V, K> keyFunction) {
        return Streams.stream(values).collect(ImmutableSetMultimap.toImmutableSetMultimap(keyFunction, Function.identity()));
    }

    /**
     * same as {@link com.google.common.collect.Multimaps#index} but returning a
     * {@link ImmutableSetMultimap} and applying function also to value.
     * 
     * @deprecated consider using the stream api directly
     */
    @Nonnull
    @Deprecated
    public static <K, V, O> ImmutableSetMultimap<K, V> indexSetMultimap(final Iterable<? extends O> values,
            final Function<? super O, K> keyFunction, final Function<? super O, V> valueFunction) {
        return Streams.stream(values).collect(ImmutableSetMultimap.toImmutableSetMultimap(keyFunction, valueFunction));
    }

    /**
     * same as {@link com.google.common.collect.Multimaps#index} but returning a
     * {@link ImmutableSetMultimap}.
     * 
     * @deprecated consider using the stream api directly
     */
    @Nonnull
    @Deprecated
    public static <K, V> ImmutableSetMultimap<K, V> indexSetMultimap(final Iterator<? extends V> values,
            final Function<? super V, K> keyFunction) {
        return Streams.stream(values).collect(ImmutableSetMultimap.toImmutableSetMultimap(keyFunction, Function.identity()));
    }

    /**
     * same as {@link com.google.common.collect.Multimaps#index} but returning a
     * {@link ImmutableSetMultimap} and applying function also to value.
     * 
     * @deprecated consider using the stream api directly
     */
    @Nonnull
    @Deprecated
    public static <K, V, O> ImmutableSetMultimap<K, V> indexSetMultimap(final Iterator<? extends O> values,
            final Function<? super O, K> keyFunction, final Function<? super O, V> valueFunction) {
        return Streams.stream(values).collect(ImmutableSetMultimap.toImmutableSetMultimap(keyFunction, valueFunction));
    }

    /**
     * same as {@link com.google.common.collect.Multimaps#index} but applying function also to value.
     * 
     * @deprecated consider using the stream api directly
     */
    @Nonnull
    @Deprecated
    public static <K, V, O> ImmutableListMultimap<K, V> indexListMultimap(final Iterable<? extends O> values,
            final Function<? super O, K> keyFunction, final Function<? super O, V> valueFunction) {
        return Streams.stream(values).collect(ImmutableListMultimap.toImmutableListMultimap(keyFunction, valueFunction));
    }

    /**
     * same as {@link com.google.common.collect.Multimaps#index} but applying function also to value.
     * 
     * @deprecated consider using the stream api directly
     */
    @Nonnull
    @Deprecated
    public static <K, V, O> ImmutableListMultimap<K, V> indexListMultimap(final Iterator<? extends O> values,
            final Function<? super O, K> keyFunction, final Function<? super O, V> valueFunction) {
        return Streams.stream(values).collect(ImmutableListMultimap.toImmutableListMultimap(keyFunction, valueFunction));
    }

    /**
     * returns a cartesian product which contains all values for all keys (and vice versa)
     */
    @Nonnull
    public static <K, V> ImmutableSetMultimap<K, V> cartesianProduct(final Set<? extends K> keys,
            final Set<? extends V> values) {
        return keys
                .stream()
                .collect(ImmutableSetMultimap.flatteningToImmutableSetMultimap(Function.identity(), i -> values.stream()));
    }

    /**
     * @deprecated this was a workaround for the missing {@link java.util.Map#forEach} method in {@link Multimap}s
     */
    @Deprecated
    public static <K, V> void forEach(final Multimap<? extends K, ? extends V> map, final BiConsumer<K, V> consumer) {
        map.forEach(consumer);
    }

    /**
     * Like {@link Multimaps#filterEntries(SetMultimap, com.google.common.base.Predicate)} but accepts a {@link BiPredicate}
     * which is much more readable.
     */
    @Nonnull
    public static <K, V> SetMultimap<K, V> filterEntries(final SetMultimap<K, V> map, final BiPredicate<K, V> predicate) {
        Preconditions.checkNotNull(predicate);
        return Multimaps.filterEntries(map, e -> predicate.test(e.getKey(), e.getValue()));
    }

    /**
     * Like {@link Multimaps#filterEntries(Multimap, com.google.common.base.Predicate)} but accepts a {@link BiPredicate}
     * which is much more readable.
     */
    @Nonnull
    public static <K, V> Multimap<K, V> filterEntries(final Multimap<K, V> map, final BiPredicate<K, V> predicate) {
        Preconditions.checkNotNull(predicate);
        return Multimaps.filterEntries(map, e -> predicate.test(e.getKey(), e.getValue()));
    }

}
