package de.justsoftware.toolbox.guava.collect;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;

/**
 * Methods which are missing in {@link com.google.common.collect.Maps}
 */
@ParametersAreNonnullByDefault
public class Maps2 {

    /**
     * transforms a caching map (caches null values with optional) to a normal map, without null keys and null values
     * 
     * @param map
     *            {@link Map} with Optinal as value
     * @return {@link ImmutableMap} without null values, null keys and absent values
     */
    @Nonnull
    public static <K, V> ImmutableMap<K, V> onlyPresentValues(final Map<? extends K, ? extends Optional<? extends V>> map) {
        final ImmutableMap.Builder<K, V> result = ImmutableMap.builder();
        map.forEach((k, ov) -> {
            if (k != null && ov != null && ov.isPresent()) {
                result.put(k, ov.get());
            }
        });
        return result.build();
    }

    /**
     * same as {@link Maps#filterValues} but casts the result
     */
    @SuppressWarnings("unchecked")
    @Nonnull
    public static <K, V> Map<K, V> filterValues(final Map<K, ?> unfiltered, final Class<V> type) {
        return (Map<K, V>) Maps.filterValues(unfiltered, type::isInstance);
    }

    /**
     * same as {@link Maps#filterKeys} but casts the result
     */
    @SuppressWarnings("unchecked")
    @Nonnull
    public static <K, V> Map<K, V> filterKeys(final Map<?, V> unfiltered, final Class<K> type) {
        return (Map<K, V>) Maps.filterKeys(unfiltered, type::isInstance);
    }

    /**
     * join multiple maps into a single map. Each key will be added only once.
     */
    @SafeVarargs
    @Nonnull
    public static <K, V> ImmutableMap<K, V> join(final Map<? extends K, ? extends V>... ms) {
        return join(Iterators.forArray(ms));
    }

    /**
     * join multiple maps into a single map. Each key will be added only once.
     */
    @Nonnull
    public static <K, V> ImmutableMap<K, V> join(final Iterable<? extends Map<? extends K, ? extends V>> ms) {
        return join(ms.iterator());
    }

    /**
     * join multiple maps into a single map. Each key will be added only once.
     */
    @Nonnull
    public static <K, V> ImmutableMap<K, V> join(final Iterator<? extends Map<? extends K, ? extends V>> ms) {
        final Set<K> s = new HashSet<>();
        final ImmutableMap.Builder<K, V> result = ImmutableMap.builder();
        ms.forEachRemaining(
                m -> m.forEach(
                        (key, value) -> {
                            if (s.add(key)) {
                                result.put(key, value);
                            }
                        }));
        return result.build();
    }

    /**
     * Like {@link Maps#filterEntries(Map, com.google.common.base.Predicate)} but accepts a {@link BiPredicate} which is much
     * more readable.
     */
    @Nonnull
    public static <K, V> Map<K, V> filterEntries(final Map<K, V> map, final BiPredicate<K, V> predicate) {
        Preconditions.checkNotNull(predicate);
        return Maps.filterEntries(map, e -> predicate.test(e.getKey(), e.getValue()));
    }

}
