/*
 * (c) Copyright 2012 Just Software AG
 * 
 * Created on 21.12.2012 by Christian Ewers <christian.ewers@juststoftwareag.com>
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
import org.joda.time.DateTimeZone;

/**
 * Interface for providing the current time to the application.
 * For production, an immutable implementation must be used. In Tests a mutable clock can be used to simulate time periods.
 * 
 * @author Christian Ewers <christian.ewers@juststoftwareag.com> (initial creation)
 */
public abstract class Clock {

    /**
     * Returns the current system time.
     */
    @Nonnull
    public abstract DateTime now();

    /**
     * Returns the current millis
     */
    public long nowMillis() {
        return now().getMillis();
    }

    /**
     * Obtains a clock that returns the current instant using the system clock, converting to date and time using the default
     * time-zone.
     */
    @Nonnull
    public static Clock systemDefaultZone() {
        return new SystemClock(DateTimeZone.getDefault());
    }

    /**
     * Obtains a clock that returns the current instant using the system clock, converting to date and time
     * using the UTC time-zone.
     */
    @Nonnull
    public static Clock systemUTC() {
        return new SystemClock(DateTimeZone.UTC);
    }

    /**
     * Implementation representing the actual system time.
     * Should be used in production.
     * 
     * @author Christian Ewers <christian.ewers@juststoftwareag.com> (initial creation)
     */
    @ParametersAreNonnullByDefault
    private static final class SystemClock extends Clock {

        private final DateTimeZone _timeZone;

        private SystemClock(final DateTimeZone timezone) {
            _timeZone = timezone;
        }

        @Override
        public DateTime now() {
            return new DateTime(_timeZone);
        }

    }
}
