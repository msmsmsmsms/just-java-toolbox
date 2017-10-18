package de.justsoftware.toolbox;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import org.mockito.AdditionalAnswers;
import org.mockito.Matchers;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Maps;

import de.justsoftware.toolbox.Calculator.Operation;

/**
 * test for {@link Calculator}
 * 
 * @author Jan Burkhardt (initial creation)
 */
@Test
@ParametersAreNonnullByDefault
@SuppressWarnings("boxing")
public class CalculatorTest {

    private static final Function<Set<Integer>, Map<Integer, Long>> SQUARE_BULK =
            integerSet -> Maps.toMap(integerSet, i -> i.longValue() * i);

    @Nonnull
    private static ImmutableSetMultimap<Integer, Long> setmultimapBulk(final Set<Integer> is) {
        final ImmutableSetMultimap.Builder<Integer, Long> result = ImmutableSetMultimap.builder();
        for (final Integer i : is) {
            for (long l = 0; l < i; l++) {
                result.put(i, l);
            }
        }
        return result.build();
    }

    @Nonnull
    private static ImmutableListMultimap<Integer, Long> listmultimapBulk(final Set<Integer> is) {
        final ImmutableListMultimap.Builder<Integer, Long> result = ImmutableListMultimap.builder();
        for (final Integer i : is) {
            for (long l = 0; l < i; l++) {
                result.put(i, l);
            }
        }
        return result.build();
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    private static <V> Consumer<V> mockConsumer() {
        return mock(Consumer.class);
    }

    public void forFunctionShouldCreateAWorkingOperation() {
        final Consumer<String> consumer = mockConsumer();
        final Calculator calculator = new Calculator();

        final Operation<Integer, String> op = calculator.forFunction(integerSet -> String::valueOf);
        op.call(10, consumer);

        verifyNoMoreInteractions(consumer);

        calculator.run();

        verify(consumer).accept("10");
        verifyNoMoreInteractions(consumer);
    }

    public void forMapShouldCreateAWorkingOperation() {
        final Consumer<Long> consumer = mockConsumer();
        final Calculator calculator = new Calculator();

        final Operation<Integer, Long> op = calculator.forMap(SQUARE_BULK);
        op.call(5, consumer);

        verifyNoMoreInteractions(consumer);

        calculator.run();

        verify(consumer).accept(25L);
        verifyNoMoreInteractions(consumer);
    }

    public void forSetMultimapShouldCreateAWorktingOperation() {
        final Consumer<Set<Long>> consumer = mockConsumer();
        final Calculator calculator = new Calculator();

        final Operation<Integer, Set<Long>> op = calculator.forSetMultimap(CalculatorTest::setmultimapBulk);
        op.call(3, consumer);

        verifyNoMoreInteractions(consumer);

        calculator.run();

        verify(consumer).accept(ImmutableSet.of(0L, 1L, 2L));
        verifyNoMoreInteractions(consumer);
    }

    public void forListMultimapShouldCreateAWorktingOperation() {
        final Consumer<List<Long>> consumer = mockConsumer();
        final Calculator calculator = new Calculator();

        final Operation<Integer, List<Long>> op = calculator.forListMultimap(CalculatorTest::listmultimapBulk);
        op.call(3, consumer);

        verifyNoMoreInteractions(consumer);

        calculator.run();

        verify(consumer).accept(ImmutableList.of(0L, 1L, 2L));
        verifyNoMoreInteractions(consumer);
    }

    public void skipNullsShouldReturnNull() {
        final Consumer<String> consumer = mockConsumer();
        final Calculator calculator = new Calculator();

        final Operation<Integer, String> op = calculator.forFunction(integerSet -> String::valueOf);
        op.skipNull();
        op.call(null, consumer);

        //because the result is already known it is evicted immediately
        verify(consumer).accept(null);

        calculator.run();

        verifyNoMoreInteractions(consumer);
    }

    public void notSkipNullShouldCallFunction() {
        final Consumer<String> consumer = mockConsumer();
        final Calculator calculator = new Calculator();

        final Operation<Integer, String> op = calculator.forFunction(integerSet -> String::valueOf);
        op.call(null, consumer);

        verifyNoMoreInteractions(consumer);

        calculator.run();

        verify(consumer).accept("null");
        verifyNoMoreInteractions(consumer);
    }

    public void transformShouldWork() {
        final Consumer<String> consumer = mockConsumer();
        final Calculator calculator = new Calculator();

        final Operation<Integer, Long> op = calculator.forMap(SQUARE_BULK);
        final Operation<Integer, String> transformed = op.transform(String::valueOf);

        transformed.call(5, consumer);
        calculator.run();

        verify(consumer).accept("25");
        verifyNoMoreInteractions(consumer);
    }

    public void combineShouldWork() {

        final Consumer<String> consumer = mockConsumer();
        final Calculator calculator = new Calculator();

        final Operation<Integer, Long> op1 = calculator.forMap(SQUARE_BULK);

        @SuppressWarnings("unchecked")
        final Operation<Long, String> op2 = spy(Operation.class, calculator.forFunction(longSet -> String::valueOf));

        final Operation<Integer, String> combined = op1.combine(op2);
        combined.call(5, consumer);
        calculator.run();

        verify(consumer).accept("25");

        verify(op2).call(eq(25L), Matchers.<Consumer<String>>any());
    }

    @Nonnull
    private <T> T spy(final Class<T> clz, final T delegate) {
        return mock(clz, AdditionalAnswers.delegatesTo(delegate));
    }

    @DataProvider
    @Nonnull
    Object[][] bulkCallShouldReturnMap() {
        return new Object[][] {
                { ImmutableSet.of(), ImmutableMap.of() },
                { ImmutableSet.of(3), ImmutableMap.of(3, 9L) },
                { ImmutableSet.of(1, 2, 3), ImmutableMap.of(1, 1L, 2, 4L, 3, 9L) },
        };
    }

    @Test(dataProvider = "bulkCallShouldReturnMap")
    public void bulkCallShouldReturnMap(final ImmutableSet<Integer> inputs, final Map<Integer, Long> expected) {
        final Consumer<Map<Integer, Long>> consumer = mockConsumer();
        final Calculator calculator = new Calculator();

        @SuppressWarnings("unchecked")
        final Function<Set<Integer>, Map<Integer, Long>> f = spy(Function.class, SQUARE_BULK);
        final Operation<Integer, Long> op = calculator.forMap(f);

        op.call(inputs, consumer);
        calculator.run();

        verify(consumer).accept(expected);
        verifyNoMoreInteractions(consumer);

        if (!inputs.isEmpty()) {
            verify(f).apply(inputs);
        }
        verifyNoMoreInteractions(f);
    }

    public void bulkCallShouldCallDelegateOnlyOnce() {
        final Consumer<Map<Integer, Long>> consumer = mockConsumer();
        final Calculator calculator = new Calculator();

        @SuppressWarnings("unchecked")
        final Function<Set<Integer>, Map<Integer, Long>> f = spy(Function.class, SQUARE_BULK);
        final Operation<Integer, Long> op = calculator.forMap(f);

        op.call(ImmutableSet.of(1, 2), consumer);
        op.call(ImmutableSet.of(1, 3), consumer);
        calculator.run();

        verify(consumer).accept(ImmutableMap.of(1, 1L, 2, 4L));
        verify(consumer).accept(ImmutableMap.of(1, 1L, 3, 9L));
        verifyNoMoreInteractions(consumer);

        verify(f).apply(ImmutableSet.of(1, 2, 3));
        verifyNoMoreInteractions(f);
    }

    public void twoParamterCallShouldWork() {
        @SuppressWarnings("unchecked")
        final BiConsumer<Long, Long> consumer = mock(BiConsumer.class);
        final Calculator calculator = new Calculator();

        @SuppressWarnings("unchecked")
        final Function<Set<Integer>, Map<Integer, Long>> f = spy(Function.class, SQUARE_BULK);
        final Operation<Integer, Long> op = calculator.forMap(f);

        op.call(6, 8, consumer);
        calculator.run();

        verify(consumer).accept(36L, 64L);
        verifyNoMoreInteractions(consumer);

        verify(f).apply(ImmutableSet.of(6, 8));
        verifyNoMoreInteractions(f);
    }

    public void twoParamterButEqualShouldCallDelegateOnlyOnce() {
        @SuppressWarnings("unchecked")
        final BiConsumer<Long, Long> consumer = mock(BiConsumer.class);
        final Calculator calculator = new Calculator();

        @SuppressWarnings("unchecked")
        final Function<Set<Integer>, Map<Integer, Long>> f = spy(Function.class, SQUARE_BULK);
        final Operation<Integer, Long> op = calculator.forMap(f);

        op.call(3, 3, consumer);
        calculator.run();

        verify(consumer).accept(9L, 9L);
        verifyNoMoreInteractions(consumer);

        verify(f).apply(ImmutableSet.of(3));
        verifyNoMoreInteractions(f);
    }

    public void callShouldCallDelegateOnlyOnce() {
        final Consumer<Long> consumer = mockConsumer();
        final Calculator calculator = new Calculator();

        @SuppressWarnings("unchecked")
        final Function<Set<Integer>, Map<Integer, Long>> f = spy(Function.class, SQUARE_BULK);
        final Operation<Integer, Long> op = calculator.forMap(f);

        op.call(7, consumer);
        op.call(7, consumer);
        op.call(7, consumer);

        calculator.run();

        verify(consumer, times(3)).accept(49L);
        verifyNoMoreInteractions(consumer);

        verify(f).apply(ImmutableSet.of(7));
        verifyNoMoreInteractions(f);
    }

}
