package de.justsoftware.toolbox.result;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A Result is either a value or an error. If can be used to return Exceptions from {@link Function}s or Visitors. This
 * class is inspired by {@link Optional} and the Result type from Rust.
 *
 * @param <V> type of the value
 * @param <E> type of the error
 *
 * @see <a href="https://doc.rust-lang.org/std/result/enum.Result.html">Result (Rust)</a>
 */
@ParametersAreNonnullByDefault
public abstract class Result<V, E> {

    private Result() {
        // Prevent additional implementations of this class
    }

    /**
     * Returns a successful result with a value.
     */
    @Nonnull
    public static <V, E> Result<V, E> ok(V v) {
        return new Ok(v);
    }

    /**
     * Returns a erroneous result with an exception.
     */
    @Nonnull
    public static <V, E> Result<V, E> err(E e) {
        return new Err(e);
    }

    /**
     * Returns a Result from a Supplier. The value which is returned by the supplier is wrapped in Result.ok(). Then the
     * supplier throws an exception it is wrapped in a Result.err().
     */
    @Nonnull
    public static <V, E extends Throwable> Result<V, E> from(Supplier<V, E> supplier) {
        try {
            return Result.ok(supplier.get());
        } catch (Throwable e) {
            return Result.err((E)e);
        }
    }

    /**
     * If the Result is ok, its value is returned. For err Results the wrapped exception is thrown.
     */
    @Nonnull
    public static <V, E extends Throwable> V getOrThrow(Result<V, E> r) throws E {
        if (r.isOk()) {
            return r.getOk().get();
        } else {
            throw r.getErr().get();
        }
    }

    /**
     * Returns true if this Result is ok.
     */
    public abstract boolean isOk();

    /**
     * When this Result is ok, its wrapped value is passed to the consumer.
     */
    public abstract void ifOk(Consumer<V> consumer);

    /**
     * Returns true if this Result is an error.
     */
    public abstract boolean isErr();

    /**
     * When this Result is an error, its wrapped exception is passed to the consumer.
     */
    public abstract void ifErr(Consumer<E> consumer);

    /**
     * Call ifOk when the Result is ok, or ifErr when the Result is an error.
     * @return the result of the called Function
     */
    @Nonnull
    public abstract <T> T accept(Function<? super V, ? extends T> ifOk, Function<? super E, ? extends T> ifErr);

    /**
     * Return the value as Optional for ok Results. Return Optional.absent() otherwise.
     */
    @Nonnull
    public abstract Optional<V> getOk();

    /**
     * Return the exception as Optional for error Results. Return Optional.absent() otherwise.
     */
    @Nonnull
    public abstract Optional<E> getErr();

    /**
     * If the Result is ok, apply the provided mapping function to it. If the Result is an error, it is returned
     * unchanged.
     */
    @Nonnull
    public abstract <U> Result<U, E> map(Function<? super V, ? extends U> mapper);

    /**
     * If the Result is ok, return the parameter. If the Result is an error, it is returned unchanged.
     */
    @Nonnull
    public abstract <U> Result<U, E> and(Result<U, E> result);

    /**
     * If the Result is ok, return the result of the mapper. If the Result is an error, it is returned unchanged.
     */
    @Nonnull
    public abstract <U, E2> Result<U, E2> andThen(Function<? super V, Result<U, E2>> mapper);

    /**
     * If the Result is an error, return the parameter. If the Result is ok, it is returned unchanged.
     */
    @Nonnull
    public abstract <U> Result<U, E> or(Result<U, E> result);

    /**
     * If the Result is an error, return the the result of the mapper. If the Result is ok, it is returned unchanged.
     */
    @Nonnull
    public abstract <U, E2> Result<U, E2> orElse(Function<? super E, Result<U, E2>> mapper);


    /**
     * A Supplier which may throw exceptions.
     * @see {@link java.util.function.Supplier}
     */
    @FunctionalInterface
    public interface Supplier<V, E extends Throwable> {
        @Nonnull
        V get() throws E;
    }


    @ParametersAreNonnullByDefault
    private static class Ok<V, E> extends Result<V, E> {

        private final V _value;

        private Ok(V value) {
            _value = value;
        }

        @Override
        public boolean isOk() {
            return true;
        }

        @Override
        public void ifOk(Consumer<V> consumer) {
            consumer.accept(_value);
        }

        @Override
        public boolean isErr() {
            return false;
        }

        @Override
        public void ifErr(Consumer<E> consumer) {
        }

        @Nonnull
        @Override
        public <T> T accept(Function<? super V, ? extends T> ifOk, Function<? super E, ? extends T> ifErr) {
            return ifOk.apply(_value);
        }

        @Override
        public Optional<V> getOk() {
            return Optional.of(_value);
        }

        @Override
        public Optional<E> getErr() {
            return Optional.empty();
        }

        @Override
        public <U> Result<U, E> map(Function<? super V, ? extends U> mapper) {
            return new Ok(mapper.apply(_value));
        }

        @Override
        public <U> Result<U, E> and(Result<U, E> result) {
            return result;
        }

        @Override
        public <U, E2> Result<U, E2> andThen(Function<? super V, Result<U, E2>> mapper) {
            return mapper.apply(_value);
        }

        @Override
        public <U> Result<U, E> or(Result<U, E> result) {
            return (Result<U, E>) this;
        }

        @Override
        public <U, E2> Result<U, E2> orElse(Function<? super E, Result<U, E2>> mapper) {
            return (Result<U, E2>) this;
        }

        @Override
        public String toString() {
            return "ok(" + _value +")";
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Ok
                && Objects.equals(((Ok)o)._value, _value);
        }

        @Override
        public int hashCode() {
            return _value.hashCode();
        }
    }


    @ParametersAreNonnullByDefault
    private static class Err<V, E> extends Result<V, E> {

        private final E _err;

        private Err(E err) {
            _err = err;
        }

        @Override
        public boolean isOk() {
            return false;
        }

        @Override
        public void ifOk(Consumer<V> consumer) {
        }

        @Override
        public boolean isErr() {
            return true;
        }

        @Override
        public void ifErr(Consumer<E> consumer) {
            consumer.accept(_err);
        }

        @Nonnull
        @Override
        public <T> T accept(Function<? super V, ? extends T> ifOk, Function<? super E, ? extends T> ifErr) {
            return ifErr.apply(_err);
        }

        @Override
        public Optional<V> getOk() {
            return Optional.empty();
        }

        @Override
        public Optional<E> getErr() {
            return Optional.of(_err);
        }

        @Override
        public <U> Result<U, E> map(Function<? super V, ? extends U> mapper) {
            return (Result<U, E>) this;
        }

        @Override
        public <U> Result<U, E> and(Result<U, E> result) {
            return (Result<U, E>) this;
        }

        @Override
        public <U, E2> Result<U, E2> andThen(Function<? super V, Result<U, E2>> mapper) {
            return (Result<U, E2>) this;
        }

        @Override
        public <U> Result<U, E> or(Result<U, E> result) {
            return result;
        }

        @Override
        public <U, E2> Result<U, E2> orElse(Function<? super E, Result<U, E2>> mapper) {
            return mapper.apply(_err);
        }

        @Override
        public String toString() {
            return "err(" + _err +")";
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Err
                    && Objects.equals(((Err)o)._err, _err);
        }

        @Override
        public int hashCode() {
            return _err.hashCode();
        }
    }
}
