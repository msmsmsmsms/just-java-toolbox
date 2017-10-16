/*
 * (c) Copyright 2015 Just Software AG
 * 
 * Created on Dec 4, 2015 by Miriam Doelle <miriam.doelle@justsoftwareag.com>
 * 
 * This file contains unpublished, proprietary trade secret information of
 * Just Software AG. Use, transcription, duplication and
 * modification are strictly prohibited without prior written consent of
 * Just Software AG.
 */
package de.justsoftware.toolbox.clock;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import org.joda.time.DateTime;
import org.joda.time.MutableDateTime;
import org.joda.time.Period;

/**
 * Mutable clock implementation for testing purpose.
 * 
 * @author Christian Ewers <christian.ewers@juststoftwareag.com> (initial creation)
 */
@ParametersAreNonnullByDefault
public final class MutableClock extends Clock {

    private MutableDateTime _now = new MutableDateTime();

    private MutableClock() {
    }

    @Override
    public DateTime now() {
        return _now != null
            ? new DateTime(_now)
            : new DateTime();
    }

    /**
     * Adds the given Period to the time presented by this clock.
     */
    @Nonnull
    public MutableClock plus(final Period period) {
        getOrCreateNow().add(period);
        return this;
    }

    /**
     * Adds the given number of seconds to this clock.
     */
    @Nonnull
    public MutableClock plusSeconds(final int seconds) {
        getOrCreateNow().addSeconds(seconds);
        return this;
    }

    /**
     * Adds the given number of minutes to this clock.
     */
    @Nonnull
    public MutableClock plusMinutes(final int minutes) {
        getOrCreateNow().addMinutes(minutes);
        return this;
    }

    /**
     * Adds the given number of days to this clock.
     */
    @Nonnull
    public MutableClock plusDays(final int days) {
        getOrCreateNow().addDays(days);
        return this;
    }

    /**
     * Adds the given number of months to this clock.
     */
    @Nonnull
    public MutableClock plusMonths(final int months) {
        getOrCreateNow().addMonths(months);
        return this;
    }

    /**
     * Adds the given number of years to this clock.
     */
    @Nonnull
    public MutableClock plusYears(final int years) {
        getOrCreateNow().addYears(years);
        return this;
    }

    /**
     * move clock to a certain time
     */
    @Nonnull
    public MutableClock setTime(final DateTime otherNow) {
        _now = otherNow.toMutableDateTime();
        return this;
    }

    /**
     * Resets the clock to the actual system time.
     */
    @Nonnull
    public MutableClock reset() {
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
    public MutableClock stop() {
        getOrCreateNow();
        return this;
    }

}
