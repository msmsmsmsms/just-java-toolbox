package de.justsoftware.toolbox.clock;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import org.joda.time.DateTime;
import org.joda.time.MutableDateTime;
import org.joda.time.ReadableDuration;
import org.joda.time.ReadablePeriod;

import java.time.Duration;

/**
 * Mutable clock implementation for testing purpose. This class is not final to be able to create a spy on it.
 * 
 * @deprecated Use {@link java.time.Clock}, especially {@link java.time.Clock#offset(java.time.Clock, Duration)}.
 * 
 * @author Christian Ewers <christian.ewers@juststoftwareag.com> (initial creation)
 */
@Deprecated
@ParametersAreNonnullByDefault
public class MutableClock implements Clock {

    private MutableDateTime _now = new MutableDateTime();

    public MutableClock() {
    }

    @Override
    public DateTime now() {
        return _now != null
            ? new DateTime(_now)
            : new DateTime();
    }

    @Nonnull
    public final MutableClock plusMillis(final long millis) {
        getOrCreateNow().add(millis);
        return this;
    }

    /**
     * Adds the given Period to the time presented by this clock.
     */
    @Nonnull
    public final MutableClock plus(final ReadablePeriod period) {
        getOrCreateNow().add(period);
        return this;
    }

    /**
     * Adds the given Duration to the time presented by this clock.
     */
    @Nonnull
    public final MutableClock plus(final ReadableDuration duration) {
        getOrCreateNow().add(duration);
        return this;
    }

    /**
     * Adds the given number of seconds to this clock.
     */
    @Nonnull
    public final MutableClock plusSeconds(final int seconds) {
        getOrCreateNow().addSeconds(seconds);
        return this;
    }

    /**
     * Adds the given number of minutes to this clock.
     */
    @Nonnull
    public final MutableClock plusMinutes(final int minutes) {
        getOrCreateNow().addMinutes(minutes);
        return this;
    }

    /**
     * Adds the given number of days to this clock.
     */
    @Nonnull
    public final MutableClock plusDays(final int days) {
        getOrCreateNow().addDays(days);
        return this;
    }

    /**
     * Adds the given number of months to this clock.
     */
    @Nonnull
    public final MutableClock plusMonths(final int months) {
        getOrCreateNow().addMonths(months);
        return this;
    }

    /**
     * Adds the given number of years to this clock.
     */
    @Nonnull
    public final MutableClock plusYears(final int years) {
        getOrCreateNow().addYears(years);
        return this;
    }

    /**
     * move clock to a certain time
     */
    @Nonnull
    public final MutableClock setTime(final DateTime otherNow) {
        _now = otherNow.toMutableDateTime();
        return this;
    }

    /**
     * Resets the clock to the actual system time.
     */
    @Nonnull
    public final MutableClock reset() {
        _now = null;
        return this;
    }

    @Nonnull
    private MutableDateTime getOrCreateNow() {
        if (_now == null) {
            _now = new MutableDateTime();
        }
        return _now;
    }

    /**
     * stop the clock at the current time
     */
    @Nonnull
    public final MutableClock stop() {
        getOrCreateNow();
        return this;
    }

}
