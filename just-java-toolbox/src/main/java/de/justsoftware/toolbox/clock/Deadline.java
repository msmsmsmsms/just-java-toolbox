package de.justsoftware.toolbox.clock;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.function.LongConsumer;
import java.util.function.LongFunction;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import com.google.common.base.MoreObjects;

/**
 * A wrapper around a {@link DateTime} (the deadlines timestamp) and its clock which can be used to track remaining time of
 * multiple timeout based operations.
 */
public class Deadline {

    private final Clock _clock;
    private final DateTime _end;

    Deadline(final Clock clock, final DateTime end) {
        _clock = clock;
        _end = end;
    }

    public boolean isTimeLeft() {
        return _end.isAfter(_clock.now());
    }

    @Nonnull
    public Duration remaining() {
        return new Duration(_clock.now(), _end);
    }

    public long remainingMillis() {
        return _end.getMillis() - _clock.nowMillis();
    }

    public boolean await(final Supplier<Condition> condition) throws InterruptedException {
        final long remainingMillis = remainingMillis();
        return remainingMillis > 0 && condition.get().await(remainingMillis, TimeUnit.MILLISECONDS);
    }

    public boolean await(final Condition condition) throws InterruptedException {
        return await(() -> condition);
    }

    /**
     * Resolves a future by calling {@link Future#get(long, TimeUnit)}.
     *
     * @throws InterruptedException
     *             this exception is forwarded from {@link Future#get(long, TimeUnit)}
     * @throws ExecutionException
     *             this exception is forwarde from {@link Future#get(long, TimeUnit)}
     * @throws TimeoutException
     *             this exception is forwarde from {@link Future#get(long, TimeUnit)} but also thrown if the deadline reaches
     *             its limit
     */
    public <T> T resolveFuture(final Future<T> future) throws InterruptedException, ExecutionException, TimeoutException {
        final long remainingMillis = remainingMillis();
        if (remainingMillis > 0) {
            return future.get(remainingMillis, TimeUnit.MILLISECONDS);
        } else {
            throw new TimeoutException();
        }
    }

    /**
     * Execute the supplied function only if time left and return its result wrapped into an optional.
     *
     * @throws NullPointerException
     *             if function returns null
     */
    @Nonnull
    public <T> Optional<T> withTimeLeft(final LongFunction<T> function) {
        final long remainingMillis = remainingMillis();
        return remainingMillis > 0
            ? Optional.of(function.apply(remainingMillis))
            : Optional.empty();
    }

    /**
     * Execute the supplied consumer only if time left.
     */
    public boolean ifTimeLeft(final LongConsumer consumer) {
        final long remainingMillis = remainingMillis();
        if (remainingMillis > 0) {
            consumer.accept(remainingMillis);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return MoreObjects
                .toStringHelper(this)
                .add("end", _end)
                .add("remainingMillis", remainingMillis())
                .toString();
    }

}
