/*
 * (c) Copyright 2016 Just Software AG
 *
 * Created on 08.12.2016 by Jan Burkhardt
 *
 * This file contains unpublished, proprietary trade secret information of
 * Just Software AG. Use, transcription, duplication and
 * modification are strictly prohibited without prior written consent of
 * Just Software AG.
 */
package de.justsoftware.toolbox;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.HashSet;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

import de.justsoftware.toolbox.Calculator.Operation;
import de.justsoftware.toolbox.function.SplitConsumer;
import de.justsoftware.toolbox.model.AbstractImmutableEqualsObject;

/**
 * a bigger test of {@link Calculator}
 * 
 * @author Jan Burkhardt (initial creation)
 */
@ParametersAreNonnullByDefault
@Test
@SuppressWarnings("boxing")
public class PascalsTriangleCalculatorTest {

    private static class BinomialCoefficient extends AbstractImmutableEqualsObject {
        private final int _n;
        private final int _k;

        BinomialCoefficient(final int n, final int k) {
            super(n, k);
            assertTrue(n >= 0);
            assertTrue(k >= 0);
            assertTrue(k <= n);
            _n = n;
            _k = k;
        }

        @Nonnull
        public ImmutableSet<BinomialCoefficient> getParents() {
            final ImmutableSet.Builder<BinomialCoefficient> result = ImmutableSet.builder();
            if (_k < _n) {
                result.add(new BinomialCoefficient(_n - 1, _k));
            }
            if (_k > 0) {
                result.add(new BinomialCoefficient(_n - 1, _k - 1));
            }
            return result.build();
        }

        @Override
        public String toString() {
            return "(" + _n + "/" + _k + ")";
        }

    }

    private void pascalsTriangle(final Operation<BinomialCoefficient, ImmutableSet<BinomialCoefficient>> getParent,
            final BinomialCoefficient bc, final Consumer<Integer> result) {
        getParent.call(bc, parents -> {
            switch (parents.size()) {
                case 0:
                    result.accept(1);
                    return;
                case 1:
                    pascalsTriangle(getParent, Iterables.getOnlyElement(parents), result);
                    return;
                case 2:
                    final SplitConsumer<Integer, Integer> sum =
                            new SplitConsumer<>((l, r) -> result.accept(l + r));
                    pascalsTriangle(getParent, Iterables.get(parents, 0), sum._left);
                    pascalsTriangle(getParent, Iterables.get(parents, 1), sum._right);
                    return;
                default:
                    fail("only 0 or 2 parents expected, got: " + parents);
            }
        });
    }

    /**
     * this tests calculates a binomial coefficient by the help of pascal's triangle
     * 
     * its not intended to be fast or able to calculate big sums, its intention is to get the ancestor of each parent only
     * once and bulked. I've selected it, because it has a hierarchy and is computable.
     */
    @Test(dataProvider = "pascalsTriangleTestDataProvider")
    public void pascalsTriangleTest(final int n, final int k, final int expected) {

        final Calculator c = new Calculator();

        final HashSet<BinomialCoefficient> loadedKeys = new HashSet<>();
        final ImmutableSet.Builder<BinomialCoefficient> allHandledBcs = ImmutableSet.builder();

        final Operation<BinomialCoefficient, ImmutableSet<BinomialCoefficient>> getParent =
                c.forFunction(s -> bc -> {
                    final ImmutableSet<BinomialCoefficient> result = bc.getParents();
                    assertTrue(loadedKeys.add(bc), "the key " + bc + " was already loaded!");
                    allHandledBcs.addAll(result).add(bc);
                    return result;
                });

        @SuppressWarnings("unchecked")
        final Consumer<Integer> consumer = mock(Consumer.class);

        pascalsTriangle(getParent, new BinomialCoefficient(n, k), consumer);
        c.run();

        verify(consumer).accept(expected);
        verifyNoMoreInteractions(consumer);

        assertEquals(loadedKeys, allHandledBcs.build());

    }

    @DataProvider
    @Nonnull
    Object[][] pascalsTriangleTestDataProvider() {
        return new Object[][] {
                { 0, 0, 1 },

                { 1, 0, 1 },
                { 1, 1, 1 },

                { 2, 0, 1 },
                { 2, 1, 2 },
                { 2, 2, 1 },

                { 3, 0, 1 },
                { 3, 1, 3 },
                { 3, 2, 3 },
                { 3, 3, 1 },

                { 4, 0, 1 },
                { 4, 1, 4 },
                { 4, 2, 6 },
                { 4, 3, 4 },
                { 4, 4, 1 },

                { 5, 0, 1 },
                { 5, 1, 5 },
                { 5, 2, 10 },
                { 5, 3, 10 },
                { 5, 4, 5 },
                { 5, 5, 1 },

                { 6, 0, 1 },
                { 6, 1, 6 },
                { 6, 2, 15 },
                { 6, 3, 20 },
                { 6, 4, 15 },
                { 6, 5, 6 },
                { 6, 6, 1 },

                { 15, 5, 3003 },

        };
    }

}
