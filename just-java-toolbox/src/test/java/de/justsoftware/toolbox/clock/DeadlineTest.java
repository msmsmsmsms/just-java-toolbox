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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.function.Supplier;

import org.joda.time.Duration;
import org.testng.annotations.Test;

@Test
public class DeadlineTest {

    public void testCreateWithMillis() {
        assertEquals(new MutableClock().deadline(10000).remainingMillis(), 10000);
    }

    public void testCreateWithTimeUnit() {
        assertEquals(new MutableClock().deadline(3, TimeUnit.SECONDS).remainingMillis(), 3000);
    }

    public void testCreateWithDuration() {
        assertEquals(new MutableClock().deadline(new Duration(1234)).remainingMillis(), 1234);
    }

    public void isTimeLeftShouldReturnTrueIfTimeoutIsPositive() {
        assertTrue(new MutableClock().deadline(1000).isTimeLeft());
    }

    public void isTimeLeftShouldReturnFalseIfTimeoutIsZero() {
        assertFalse(new MutableClock().deadline(0).isTimeLeft());
    }

    public void isTimeLeftShouldReturnTrueIfTimeoutIsNegative() {
        assertFalse(new MutableClock().deadline(-1).isTimeLeft());
    }

    public void remainingMillisShouldReturnInitialTimeout() {
        assertEquals(new MutableClock().deadline(1000).remainingMillis(), 1000);
    }

    public void remainingMillisShouldHonorElapsedTime() {
        final MutableClock clock = new MutableClock();

        final Deadline deadline = clock.deadline(1000);

        clock.plusMillis(500);

        assertEquals(deadline.remainingMillis(), 500);
    }

    public void remainingShouldHonorElapsedTime() {
        final MutableClock clock = new MutableClock();

        final Deadline deadline = clock.deadline(1000);

        clock.plusMillis(500);

        assertEquals(deadline.remaining(), new Duration(500));
    }

    public void awaitShouldBeCalledWithInitialTime() throws InterruptedException {
        final Condition condition = mock(Condition.class);
        when(condition.await(anyLong(), any())).thenReturn(true);

        assertTrue(new MutableClock().deadline(1000).await(condition));

        verify(condition).await(1000, TimeUnit.MILLISECONDS);
    }

    public void awaitShouldBeCalledWithRemainingTime() throws InterruptedException {
        final Condition condition = mock(Condition.class);
        when(condition.await(anyLong(), any())).thenReturn(true);

        final MutableClock clock = new MutableClock();

        final Deadline deadline = clock.deadline(1000);

        clock.plusMillis(500);

        assertTrue(deadline.await(condition));

        verify(condition).await(500, TimeUnit.MILLISECONDS);
    }

    public void awaitShouldNotBeCalledWhenTimeIsUp() throws InterruptedException {
        final Condition condition = mock(Condition.class);

        assertFalse(new MutableClock().deadline(-1).await(condition));

        verifyZeroInteractions(condition);
    }

    public void awaitShouldNotCallSupplierWhenTimeIsUp() throws InterruptedException {
        @SuppressWarnings("unchecked")
        final Supplier<Condition> conditionSupplier = mock(Supplier.class);

        assertFalse(new MutableClock().deadline(-1).await(conditionSupplier));

        verifyZeroInteractions(conditionSupplier);
    }

    public void awaitShouldCallSupplierWithInitialTime() throws InterruptedException {
        final Condition condition = mock(Condition.class);
        when(condition.await(anyLong(), any())).thenReturn(true);

        assertTrue(new MutableClock().deadline(1000).await(() -> condition));

        verify(condition).await(1000, TimeUnit.MILLISECONDS);
    }

}
