package de.justsoftware.toolbox.clock;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.ReadableDuration;

/**
 * Interface for providing the current time to the application.
 * For production, an immutable implementation must be used. In Tests a mutable clock can be used to simulate time periods.
 *
 * @deprecated Use {@link java.time.Clock} instead.
 * 
 * @author Christian Ewers <christian.ewers@juststoftwareag.com> (initial creation)
 */
@FunctionalInterface
@ParametersAreNonnullByDefault
@Deprecated
public interface Clock {

    /**
     * Returns the current system time.
     */
    @Nonnull
    DateTime now();

    /**
     * Returns the current system time as {@link Date}
     */
    @Nonnull
    default Date nowDate() {
        return now().toDate();
    }

    /**
     * Returns the current millis
     */
    default long nowMillis() {
        return now().getMillis();
    }

    /**
     * Obtains a clock that returns the current instant using the system clock, converting to date and time using the default
     * time-zone.
     */
    @Nonnull
    static Clock systemDefaultZone() {
        return forTimezone(DateTimeZone.getDefault());
    }

    /**
     * Obtains a clock that returns the current instant using the system clock, converting to date and time
     * using the UTC time-zone.
     */
    @Nonnull
    static Clock systemUTC() {
        return forTimezone(DateTimeZone.UTC);
    }

    /**
     * Obtains a clock that returns the current instant using the system clock, but with a specific timezone.
     */
    @Nonnull
    static Clock forTimezone(final DateTimeZone timezone) {
        return () -> new DateTime(timezone);
    }

}
