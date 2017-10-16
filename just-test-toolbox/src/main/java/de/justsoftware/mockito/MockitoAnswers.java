/*
 * (c) Copyright 2015 Just Software AG
 * 
 * Created on 09.11.2015 by Jan Burkhardt (jan.burkhardt@just.social)
 * 
 * This file contains unpublished, proprietary trade secret information of
 * Just Software AG. Use, transcription, duplication and
 * modification are strictly prohibited without prior written consent of
 * Just Software AG.
 */
package de.justsoftware.mockito;

import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Table;

/**
 * This is a class with useful Mockito answers.
 * 
 * @author Jan Burkhardt (jan.burkhardt@just.social) (initial creation)
 */
@ParametersAreNonnullByDefault
public enum MockitoAnswers implements Answer<Object> {

        /**
         * mock a builder which returns itself on every method call where it is possible
         */
        THIS {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable {
                final Class<?> clz = invocation.getMethod().getReturnType();
                final Object mock = invocation.getMock();
                if (clz.isInstance(mock)) {
                    return mock;
                } else {
                    return Mockito.RETURNS_DEFAULTS.answer(invocation);
                }
            }
        },

    ;

    @Nonnull
    private static <K> ImmutableSet<K> argumentAsSet(final InvocationOnMock invocation) {
        @SuppressWarnings("unchecked")
        final Iterable<K> keys = invocation.getArgumentAt(0, Iterable.class);
        // keys is null when Mockito.any() is passed as argument
        if (keys == null) {
            return ImmutableSet.of();
        }
        return ImmutableSet.copyOf(keys);
    }

    @Nonnull
    private static <K, V> ImmutableSetMultimap<K, V> argAsMultimap(final InvocationOnMock invocation) {
        @SuppressWarnings("unchecked")
        final Multimap<K, V> keys = invocation.getArgumentAt(0, Multimap.class);
        // keys is null when Mockito.any() is passed as argument
        if (keys == null) {
            return ImmutableSetMultimap.of();
        }
        return ImmutableSetMultimap.copyOf(keys);
    }

    /**
     * Create an answer to mock bulk getters. Use this to mock methods like:
     * 
     * Map<K, V> getByFoos(Iterable<K> foos);
     */
    @Nonnull
    public static <K, V> Answer<ImmutableMap<? super K, ? extends V>> bulkMapAnswer(final Function<? super K, V> f) {
        return new Answer<ImmutableMap<? super K, ? extends V>>() {
            @Override
            public ImmutableMap<K, V> answer(final InvocationOnMock invocation) {
                return ImmutableMap.copyOf(Maps.filterValues(
                        Maps.asMap(MockitoAnswers.<K>argumentAsSet(invocation), f),
                        Predicates.notNull()));
            }
        };
    }

    /**
     * Create an answer to mock bulk getters. Use this to mock methods like:
     * 
     * Map<K, V> getByFoos(Iterable<K> foos);
     */
    @Nonnull
    public static <K, V> Answer<ImmutableMap<? super K, ? extends V>> bulkMapAnswer(final Map<K, V> map) {
        return bulkMapAnswer(Functions.forMap(map, null));
    }

    /**
     * Create an answer to mock bulk getters. Use this to mock methods like:
     * 
     * ImmutableSetMultimap<K, V> getByFoos(Iterable<K> foos);
     */
    @Nonnull
    public static <K, V> Answer<ImmutableSetMultimap<? super K, ? extends V>> bulkSetMultimapAnswer(
            final Function<? super K, ? extends Iterable<? extends V>> f) {
        return invocation -> {
            final ImmutableSetMultimap.Builder<K, V> result = ImmutableSetMultimap.builder();
            for (final K key : MockitoAnswers.<K>argumentAsSet(invocation)) {
                result.putAll(key, f.apply(key));
            }
            return result.build();
        };
    }

    /**
     * Create an answer to mock bulk getters. Use this to mock methods like:
     * 
     * ImmutableSetMultmap<K, V> getByFoos(Iterable<K> foos);
     */
    @Nonnull
    public static <K, V> Answer<ImmutableSetMultimap<? super K, ? extends V>> bulkSetMultimapAnswer(
            final Multimap<K, V> map) {
        return bulkSetMultimapAnswer(map::get);
    }

    /**
     * Create an answer to mock bulk getters. Use this to mock methods like:
     * 
     * ImmutableListMultimap<K, V> getByFoos(Iterable<K> foos);
     */
    @Nonnull
    public static <K, V> Answer<ImmutableListMultimap<? super K, ? extends V>> bulkListMultimapAnswer(
            final Function<? super K, ? extends Iterable<? extends V>> f) {
        return invocation -> {
            final ImmutableListMultimap.Builder<K, V> result = ImmutableListMultimap.builder();
            for (final K key : MockitoAnswers.<K>argumentAsSet(invocation)) {
                result.putAll(key, f.apply(key));
            }
            return result.build();
        };
    }

    /**
     * Create an answer to mock bulk getters. Use this to mock methods like:
     * 
     * ImmutableListMultimap<K, V> getByFoos(Iterable<K> foos);
     */
    @Nonnull
    public static <K, V> Answer<ImmutableListMultimap<? super K, ? extends V>> bulkListMultimapAnswer(
            final Multimap<K, V> map) {
        return bulkListMultimapAnswer(map::get);
    }

    /**
     * Create an answer to mock bulk getters. Use this to mock methods like:
     * 
     * ImmutableSetMultmap<K, V> getByFoos(SetMultimap<K, V> foos);
     */
    @Nonnull
    public static <K, V> Answer<ImmutableSetMultimap<? super K, ? extends V>> filterSetMultimapAnswer(
            final Predicate<? super Entry<K, V>> predicate) {
        return new Answer<ImmutableSetMultimap<? super K, ? extends V>>() {
            @Override
            public ImmutableSetMultimap<K, V> answer(final InvocationOnMock invocation) {
                return ImmutableSetMultimap.copyOf(
                        Multimaps.filterEntries(MockitoAnswers.<K, V>argAsMultimap(invocation), predicate));
            }

        };
    }

    /**
     * Create an answer to mock bulk getters. Use this to mock methods like:
     * 
     * Table<R, C, V> getByFoos(SetMultimap<R,C> foos);
     */
    @Nonnull
    public static <C, R, V> Answer<ImmutableTable<? super C, ? super R, ? extends V>> bulkTableAnswer(
            final Function<? super Entry<C, R>, ? extends V> f) {
        return new Answer<ImmutableTable<? super C, ? super R, ? extends V>>() {
            @Override
            public ImmutableTable<? super C, ? super R, ? extends V> answer(final InvocationOnMock invocation) {
                final ImmutableTable.Builder<C, R, V> result = ImmutableTable.builder();
                for (final Entry<C, R> key : MockitoAnswers.<C, R>argAsMultimap(invocation).entries()) {
                    final V value = f.apply(key);
                    if (value != null) {
                        result.put(key.getKey(), key.getValue(), value);
                    }
                }
                return result.build();
            }
        };
    }

    /**
     * Create an answer to mock bulk getters. Use this to mock methods like:
     * 
     * Table<R, C, V> getByFoos(SetMultimap<R,C> foos);
     */
    @Nonnull
    public static <C, R, V> Answer<ImmutableTable<? super C, ? super R, ? extends V>> bulkTableAnswer(
            final BiFunction<? super C, ? super R, ? extends V> f) {
        return bulkTableAnswer(e -> f.apply(e.getKey(), e.getValue()));
    }

    /**
     * Create an answer to mock bulk getters. Use this to mock methods like:
     * 
     * Table<R, C, V> getByFoos(SetMultimap<R,C> foos);
     */
    @Nonnull
    public static <C, R, V> Answer<ImmutableTable<? super C, ? super R, ? extends V>> bulkTableAnswer(
            final Table<? super C, ? super R, ? extends V> table) {
        return bulkTableAnswer(input -> input != null
            ? table.get(input.getKey(), input.getValue())
            : null);
    }

}
