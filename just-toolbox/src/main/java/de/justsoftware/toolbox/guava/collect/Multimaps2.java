/*
 * (c) Copyright 2015 Just Software AG
 * 
 * Created on 23.10.2015 by wolfgang
 * 
 * This file contains unpublished, proprietary trade secret information of
 * Just Software AG. Use, transcription, duplication and
 * modification are strictly prohibited without prior written consent of
 * Just Software AG.
 */
package de.justsoftware.toolbox.guava.collect;

import java.util.Iterator;
import java.util.Set;
import java.util.function.BiConsumer;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Multimap;

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
     * same as {@link com.google.common.collect.Multimaps#index(Iterable, Function)} but returning a
     * {@link ImmutableSetMultimap}.
     */
    @Nonnull
    public static <K, V> ImmutableSetMultimap<K, V> indexSetMultimap(final Iterable<? extends V> values,
            final Function<? super V, K> keyFunction) {
        return indexSetMultimap(values.iterator(), keyFunction);
    }

    /**
     * same as {@link com.google.common.collect.Multimaps#index(Iterable, Function)} but returning a
     * {@link ImmutableSetMultimap} and applying function also to value.
     */
    @Nonnull
    public static <K, V, O> ImmutableSetMultimap<K, V> indexSetMultimap(final Iterable<? extends O> values,
            final Function<? super O, K> keyFunction, final Function<? super O, V> valueFunction) {
        return indexSetMultimap(values.iterator(), keyFunction, valueFunction);
    }

    /**
     * same as {@link com.google.common.collect.Multimaps#index(Iterator, Function)} but returning a
     * {@link ImmutableSetMultimap}.
     */
    @Nonnull
    public static <K, V> ImmutableSetMultimap<K, V> indexSetMultimap(final Iterator<? extends V> values,
            final Function<? super V, K> keyFunction) {
        return indexSetMultimap(values, keyFunction, Functions.<V>identity());
    }

    /**
     * same as {@link com.google.common.collect.Multimaps#index(Iterator, Function)} but returning a
     * {@link ImmutableSetMultimap} and applying function also to value.
     */
    @Nonnull
    public static <K, V, O> ImmutableSetMultimap<K, V> indexSetMultimap(final Iterator<? extends O> values,
            final Function<? super O, K> keyFunction, final Function<? super O, V> valueFunction) {
        return indexMultimap(values, keyFunction, valueFunction, ImmutableSetMultimap.<K, V>builder()).build();
    }

    /**
     * same as {@link com.google.common.collect.Multimaps#index(Iterable, Function)} but applying function also to value.
     */
    @Nonnull
    public static <K, V, O> ImmutableListMultimap<K, V> indexListMultimap(final Iterable<? extends O> values,
            final Function<? super O, K> keyFunction, final Function<? super O, V> valueFunction) {
        return indexListMultimap(values.iterator(), keyFunction, valueFunction);
    }

    /**
     * same as {@link com.google.common.collect.Multimaps#index(Iterator, Function)} but applying function also to value.
     */
    @Nonnull
    public static <K, V, O> ImmutableListMultimap<K, V> indexListMultimap(final Iterator<? extends O> values,
            final Function<? super O, K> keyFunction, final Function<? super O, V> valueFunction) {
        return indexMultimap(values, keyFunction, valueFunction, ImmutableListMultimap.<K, V>builder()).build();
    }

    @Nonnull
    private static <O, K, V, B extends Builder<K, V>> B indexMultimap(final Iterator<? extends O> values,
            final Function<? super O, K> keyFunction, final Function<? super O, V> valueFunction, final B builder) {
        Preconditions.checkNotNull(keyFunction);
        Preconditions.checkNotNull(valueFunction);
        while (values.hasNext()) {
            final O value = values.next();
            Preconditions.checkNotNull(value);
            builder.put(keyFunction.apply(value), valueFunction.apply(value));
        }
        return builder;
    }

    /**
     * returns a cartesian product which contains all values for all keys (and vice versa)
     */
    @Nonnull
    public static <K, V> ImmutableSetMultimap<K, V> cartesianProduct(final Set<? extends K> keys,
            final Set<? extends V> values) {
        final ImmutableSetMultimap.Builder<K, V> result = ImmutableSetMultimap.builder();
        keys.forEach(k -> result.putAll(k, values));
        return result.build();
    }

    /**
     * a workaround the missing {@link java.util.Map#forEach} method in {@link Multimap}s
     */
    public static <K, V> void forEach(final Multimap<? extends K, ? extends V> map, final BiConsumer<K, V> consumer) {
        map.entries().forEach(e -> consumer.accept(e.getKey(), e.getValue()));
    }

}
