package de.justsoftware.toolbox.testng;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;

/**
 * Utility for {@link org.testng.annotations.DataProvider}s.
 *
 * @author Jan Burkhardt (initial creation)
 */
public final class DataProviders {

    private DataProviders() {
        super();
    }

    private abstract static class DataProviderIterator<T> implements Iterator<Object[]> {

        private final Iterator<? extends T> _iterator;

        private DataProviderIterator(@Nonnull final Iterator<? extends T> iterator) {
            _iterator = iterator;
        }

        @Override
        public final boolean hasNext() {
            return _iterator.hasNext();
        }

        @Override
        public final Object[] next() {
            return next(_iterator.next());
        }

        @Nonnull
        protected abstract Object[] next(@Nullable T next);

        @Override
        public final void remove() {
            throw new UnsupportedOperationException();
        }
    }

    @Nonnull
    public static Iterator<Object[]> provideIterator(@Nonnull final Iterator<?> iterator) {
        return new DataProviderIterator<Object>(iterator) {

            @Override
            protected Object[] next(final Object next) {
                return new Object[] { next };
            }

        };
    }

    @Nonnull
    public static Iterator<Object[]> provideIterable(@Nonnull final Iterable<?> iterable) {
        return provideIterator(iterable.iterator());
    }

    @Nonnull
    public static <T> Iterator<Object[]> provideArray(@Nonnull final T[] objects) {
        return provideIterator(Iterators.forArray(objects));
    }

    @Nonnull
    public static Iterator<Object[]> provideVarargs(@Nonnull final Object... objects) {
        return provideArray(objects);
    }

    @Nonnull
    public static <T extends Entry<?, ?>> Iterator<Object[]> provideEntryIterator(@Nonnull final Iterator<T> iterator) {
        return new DataProviderIterator<T>(iterator) {

            @Override
            protected Object[] next(final T next) {
                if (next == null) {
                    return new Object[] { null, null };
                }
                return new Object[] { next.getKey(), next.getValue() };
            }

        };
    }

    @Nonnull
    public static Iterator<Object[]> provideEntrySet(@Nonnull final Iterable<? extends Entry<?, ?>> iterable) {
        return provideEntryIterator(iterable.iterator());
    }

    @Nonnull
    public static Iterator<Object[]> provideMap(@Nonnull final Map<?, ?> map) {
        return provideEntrySet(map.entrySet());
    }

    /**
     * produces a cartesian product of the provided sets, see {@link Sets#cartesianProduct#cartesianProduct} for
     * more info
     */
    @Nonnull
    public static Iterator<Object[]> cartesianProduct(@Nonnull final Set<?>... sets) {
        return FluentIterable.from(Sets.cartesianProduct(sets)).transform(List::toArray).iterator();
    }

}
