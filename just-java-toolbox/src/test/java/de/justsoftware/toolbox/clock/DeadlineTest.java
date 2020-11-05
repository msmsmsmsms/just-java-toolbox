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
        final Deadline deadline = Deadline.inMillis(FIXED_CLOCK, 10_000);
        assertEquals(deadline.remainingMillis(FIXED_CLOCK), 10_000);
        assertEquals(deadline.remaining(FIXED_CLOCK), Duration.ofMillis(10_000));
    }

    public void isTimeLeftShouldReturnTrueIfTimeoutIsPositive() {
        assertTrue(Deadline.inMillis(FIXED_CLOCK, 1_000).isTimeLeft(FIXED_CLOCK));
    }

    public void isTimeLeftShouldReturnFalseIfTimeoutIsZero() {
        assertFalse(Deadline.inMillis(FIXED_CLOCK, 0).isTimeLeft(FIXED_CLOCK));
    }

    public void isTimeLeftShouldReturnTrueIfTimeoutIsNegative() {
        assertFalse(Deadline.inMillis(FIXED_CLOCK, -1).isTimeLeft(FIXED_CLOCK));
    }

    public void remainingMillisShouldHonorElapsedTime() {
        final Clock offset = Clock.offset(FIXED_CLOCK, Duration.ofMillis(500));
        final Deadline deadline = Deadline.inMillis(FIXED_CLOCK, 1_000);
        assertEquals(deadline.remainingMillis(offset), 500);
    }

    public void remainingShouldHonorElapsedTime() {
        final Clock offset = Clock.offset(FIXED_CLOCK, Duration.ofMillis(500));
        final Deadline deadline = Deadline.inMillis(FIXED_CLOCK, 1_000);
        assertEquals(deadline.remaining(offset), Duration.ofMillis(500));
    }

    public void awaitShouldBeCalledWithTimeRemaining() throws InterruptedException {
        final Condition condition = mock(Condition.class);
        when(condition.await(anyLong(), any())).thenReturn(true);

        assertTrue(Deadline.inMillis(FIXED_CLOCK, 100).await(FIXED_CLOCK, () -> condition));

        verify(condition).await(100, TimeUnit.MILLISECONDS);
    }

    public void awaitShouldBeCalledWithRemainingTime() throws InterruptedException {
        final Condition condition = mock(Condition.class);
        when(condition.await(anyLong(), any())).thenReturn(true);

        final Deadline deadline = Deadline.inMillis(FIXED_CLOCK, 100);

        assertTrue(deadline.await(Clock.offset(FIXED_CLOCK, Duration.ofMillis(50)), () -> condition));

        verify(condition).await(50, TimeUnit.MILLISECONDS);
    }

    public void awaitShouldNotBeCalledWhenTimeIsUp() throws InterruptedException {
        final Condition condition = mock(Condition.class);

        assertFalse(Deadline.inMillis(FIXED_CLOCK, -1).await(FIXED_CLOCK, () -> condition));

        verifyZeroInteractions(condition);
    }

    public void awaitShouldNotCallSupplierWhenTimeIsUp() throws InterruptedException {
        @SuppressWarnings("unchecked")
        final Supplier<Condition> conditionSupplier = mock(Supplier.class);

        assertFalse(Deadline.inMillis(FIXED_CLOCK, -1).await(FIXED_CLOCK, conditionSupplier));

        verifyZeroInteractions(conditionSupplier);
    }

}
