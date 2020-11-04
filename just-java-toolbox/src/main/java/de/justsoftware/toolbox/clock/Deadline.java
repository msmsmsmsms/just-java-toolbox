package de.justsoftware.toolbox.clock;

import com.google.common.base.MoreObjects;

import javax.annotation.Nonnull;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.function.LongConsumer;
import java.util.function.LongFunction;
import java.util.function.Supplier;

/**
 * A wrapper around an {@link Instant} and a clock which can be used to track remaining time until the instant is reached.
 * Supports multiple timeout based operations.
 */
public class Deadline {

    private final Clock _clock;
    private final Instant _end;

    public Deadline(final Clock clock, final Duration durationFromNow) {
        _clock = clock;
        _end = Instant.now(clock).plus(durationFromNow);
    }

    public boolean isTimeLeft() {
        return _end.isAfter(_clock.instant());
    }

    @Nonnull
    public Duration remaining() {
        return remaining(_clock);
    }

    public Duration remaining(final Clock clock) {
        return Duration.between(clock.instant(), _end);
    }

    public long remainingMillis() {
        return remainingMillis(_clock);
    }
    
    public long remainingMillis(Clock clock) {
        return clock.instant().until(_end, ChronoUnit.MILLIS);
    }

    public boolean await(final Clock clock, final Supplier<Condition> condition) throws InterruptedException {
        final long remainingMillis = remainingMillis(clock);
        return remainingMillis > 0 && condition.get().await(remainingMillis, TimeUnit.MILLISECONDS);
    }

    public boolean await(final Condition condition) throws InterruptedException {
        return await(_clock, () -> condition);
    }

    /**
     * Resolves a future by calling {@link Future#get(long, TimeUnit)}.
     *
     * @throws InterruptedException this exception is forwarded from {@link Future#get(long, TimeUnit)}
     * @throws ExecutionException   this exception is forwarde from {@link Future#get(long, TimeUnit)}
     * @throws TimeoutException     this exception is forwarde from {@link Future#get(long, TimeUnit)} but also thrown if the deadline reaches
     *                              its limit
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
     * @throws NullPointerException if function returns null
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
