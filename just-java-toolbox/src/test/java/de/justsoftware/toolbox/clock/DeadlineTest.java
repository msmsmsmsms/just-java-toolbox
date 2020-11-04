package de.justsoftware.toolbox.clock;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoField;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.function.Supplier;

import org.testng.annotations.Test;

@Test
public class DeadlineTest {

    public static final Clock FIXED_CLOCK = Clock.fixed(Instant.now(), ZoneId.systemDefault());

    public void testCreateWithMillis() {
        final Deadline deadline = new Deadline(FIXED_CLOCK, Duration.ofMillis(10_000));
        assertEquals(deadline.remainingMillis(), 10_000);
        assertEquals(deadline.remaining(), Duration.ofMillis(10_000));
    }

    public void isTimeLeftShouldReturnTrueIfTimeoutIsPositive() {
        assertTrue(new Deadline(FIXED_CLOCK, Duration.ofMillis(1_000)).isTimeLeft());
    }

    public void isTimeLeftShouldReturnFalseIfTimeoutIsZero() {
        assertFalse(new Deadline(FIXED_CLOCK, Duration.ofMillis(0)).isTimeLeft());
    }

    public void isTimeLeftShouldReturnTrueIfTimeoutIsNegative() {
        assertFalse(new Deadline(FIXED_CLOCK, Duration.ofMillis(-1)).isTimeLeft());
    }

    public void remainingMillisShouldHonorElapsedTime() {
        final Clock offset = Clock.offset(FIXED_CLOCK, Duration.ofMillis(500));
        final Deadline deadline = new Deadline(FIXED_CLOCK, Duration.ofMillis(1_000));
        assertEquals(deadline.remainingMillis(offset), 500);
    }

    public void remainingShouldHonorElapsedTime() {
        final Clock offset = Clock.offset(FIXED_CLOCK, Duration.ofMillis(500));
        final Deadline deadline = new Deadline(FIXED_CLOCK, Duration.ofMillis(1_000));
        assertEquals(deadline.remaining(offset), Duration.ofMillis(500));
    }

    public void awaitShouldBeCalledWithTimeRemaining() throws InterruptedException {
        final Condition condition = mock(Condition.class);
        when(condition.await(anyLong(), any())).thenReturn(true);

        assertTrue(new Deadline(FIXED_CLOCK, Duration.ofMillis(100)).await(condition));

        verify(condition).await(100, TimeUnit.MILLISECONDS);
    }

    public void awaitShouldBeCalledWithRemainingTime() throws InterruptedException {
        final Condition condition = mock(Condition.class);
        when(condition.await(anyLong(), any())).thenReturn(true);

        final Deadline deadline = new Deadline(FIXED_CLOCK, Duration.ofMillis(100));

        assertTrue(deadline.await(Clock.offset(FIXED_CLOCK, Duration.ofMillis(50)), () -> condition));

        verify(condition).await(50, TimeUnit.MILLISECONDS);
    }

    public void awaitShouldNotBeCalledWhenTimeIsUp() throws InterruptedException {
        final Condition condition = mock(Condition.class);

        assertFalse(new Deadline(FIXED_CLOCK, Duration.ofMillis(-1)).await(condition));

        verifyZeroInteractions(condition);
    }

    public void awaitShouldNotCallSupplierWhenTimeIsUp() throws InterruptedException {
        @SuppressWarnings("unchecked")
        final Supplier<Condition> conditionSupplier = mock(Supplier.class);

        assertFalse(new Deadline(FIXED_CLOCK, Duration.ofMillis(-1)).await(FIXED_CLOCK, conditionSupplier));

        verifyZeroInteractions(conditionSupplier);
    }

}
