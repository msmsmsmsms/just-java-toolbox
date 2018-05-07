package de.justsoftware.toolbox;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;

import de.justsoftware.toolbox.function.SplitConsumer;

/**
 * Container for recursive and dependent calculations. Extend this class to hold intermediate results as state.
 * Also create operations you are using for your calculation as member variables.
 * 
 * @author Jan Burkhardt (initial creation)
 */
@ParametersAreNonnullByDefault
public class Calculator {

    /**
     * a operation object which wraps around a bulk method, which returns objects from database
     * 
     * @author Jan Burkhardt (initial creation)
     * @param <K>
     *            the type of the keys
     * @param <V>
     *            the type of the results
     */
    @FunctionalInterface
    @ParametersAreNonnullByDefault
    public interface Operation<K, V> {

        /**
         * get a result for a single key
         */
        void call(@Nullable final K input, final Consumer<? super V> consumer);

        /**
         * get results for multiple keys
         */
        default void call(final Set<K> inputs, final Consumer<Map<K, V>> consumer) {
            switch (inputs.size()) {
                case 0:
                    consumer.accept(Collections.emptyMap());
                    return;
                case 1:
                    final K key = Iterables.getOnlyElement(inputs);
                    call(key, r -> consumer.accept(Collections.singletonMap(key, r)));
                    return;
            }
            final Map<K, V> result = new HashMap<>();
            for (final K k : inputs) {
                call(k, r -> {
                    result.put(k, r);
                    if (inputs.equals(result.keySet())) {
                        consumer.accept(Collections.unmodifiableMap(result));
                    }
                });

            }
        }

        /**
         * get results for 2 keys
         */
        default void call(@Nullable final K k1, @Nullable final K k2, final BiConsumer<V, V> consumer) {
            if (Objects.equal(k1, k2)) {
                call(k1, r -> consumer.accept(r, r));
            } else {
                final SplitConsumer<V, V> j = new SplitConsumer<>(consumer);
                call(k1, j._left);
                call(k2, j._right);
            }
        }

        /**
         * combine this operation with another operation which transforms the value into another object
         */
        @Nonnull
        default <O> Operation<K, O> combine(final Operation<V, O> other) {
            return (k, c) -> call(k, r -> other.call(r, c));
        }

        /**
         * combine this operation with a function which transforms the result into another object
         */
        @Nonnull
        default <O> Operation<K, O> transform(final Function<V, O> f) {
            return (k, c) -> call(k, r -> c.accept(f.apply(r)));
        }

        /**
         * some operations support setting special behavior for null keys, which will always return null, because the
         * underlying methods doesn't support null
         */
        @Nonnull
        default Operation<K, V> skipNull() {
            throw new UnsupportedOperationException();
        }

    }

    @ParametersAreNonnullByDefault
    private static final class RegisteredOperation<K, V> implements Operation<K, V> {

        private final Function<? super Set<K>, Function<K, V>> _delegate;
        private final Map<Optional<K>, Optional<V>> _knownResults = new HashMap<>();
        private ImmutableListMultimap.Builder<Optional<K>, Consumer<? super V>> _missing = ImmutableListMultimap.builder();

        private RegisteredOperation(final Function<? super Set<K>, Function<K, V>> delegate) {
            _delegate = delegate;
        }

        @Override
        public void call(final K input, final Consumer<? super V> consumer) {
            final Optional<K> k = Optional.fromNullable(input);
            final Optional<V> r = _knownResults.get(k);
            if (r != null) {
                consumer.accept(r.orNull());
            } else {
                _missing.put(k, consumer);
            }
        }

        public boolean loadMissing() {
            final ImmutableListMultimap<Optional<K>, Consumer<? super V>> missing = _missing.build();
            if (missing.isEmpty()) {
                return false;
            }

            _missing = ImmutableListMultimap.builder();
            final Function<K, V> result =
                    _delegate.apply(Sets.newHashSet(Iterables.transform(missing.keySet(), Optional::orNull)));

            missing.keySet().forEach(k -> _knownResults.put(k, Optional.fromNullable(result.apply(k.orNull()))));

            for (final Entry<Optional<K>, Collection<Consumer<? super V>>> e : missing.asMap().entrySet()) {
                final Optional<V> m = _knownResults.get(e.getKey());
                e.getValue().forEach(c -> c.accept(m.orNull()));
            }

            return true;
        }

        @Override
        @Nonnull
        public RegisteredOperation<K, V> skipNull() {
            _knownResults.put(Optional.absent(), Optional.absent());
            return this;
        }

    }

    private final ArrayList<RegisteredOperation<?, ?>> _registeredOperations = new ArrayList<>();

    /**
     * create an {@link Operation} for a bulk method which returns a function. the purpose of this method is for internal use,
     * but it can be used if you have your own data structure.
     */
    @Nonnull
    public <K, V> Operation<K, V> forFunction(final Function<? super Set<K>, Function<K, V>> f) {
        final RegisteredOperation<K, V> result = new RegisteredOperation<>(f);
        _registeredOperations.add(result);
        return result;
    }

    /**
     * create an {@link Operation} for a bulk method which returns a {@link Map}
     */
    @Nonnull
    public <K, V> Operation<K, V> forMap(final Function<? super Set<K>, ? extends Map<K, V>> f) {
        return forFunction(s -> {
            final Map<K, V> m = f.apply(s);
            return m::get;
        });
    }

    /**
     * create an {@link Operation} for a bulk method which returns a {@link SetMultimap}
     */
    @Nonnull
    public <K, V> Operation<K, Set<V>> forSetMultimap(final Function<? super Set<K>, ? extends SetMultimap<K, V>> f) {
        return forFunction(s -> {
            final SetMultimap<K, V> m = f.apply(s);
            return m::get;
        });
    }

    /**
     * create an {@link Operation} for a bulk method which returns a {@link ListMultimap}
     */
    @Nonnull
    public <K, V> Operation<K, List<V>> forListMultimap(
            final Function<? super Set<K>, ? extends ListMultimap<K, V>> f) {
        return forFunction(s -> {
            final ListMultimap<K, V> m = f.apply(s);
            return m::get;
        });
    }

    /**
     * if you have initialized your calculation and have inserted your first calls then call this method which executes all
     * pending operations until all are done
     */
    public void run() {
        while (_registeredOperations.stream().filter(RegisteredOperation::loadMissing).count() > 0) {

        }
    }

}
