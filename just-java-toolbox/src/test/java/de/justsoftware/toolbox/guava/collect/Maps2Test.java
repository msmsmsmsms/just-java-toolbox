package de.justsoftware.toolbox.guava.collect;

import static org.testng.Assert.assertEquals;

import java.io.Serializable;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * tests for {@link Maps2}
 */
@Test
@ParametersAreNonnullByDefault
public class Maps2Test {

    @DataProvider
    @Nonnull
    Object[][] joinTestDataprovider() {
        return new Object[][] {
                { ImmutableList.of(ImmutableMap.of(), ImmutableMap.of()), ImmutableMap.of() },
                { ImmutableList.of(ImmutableMap.of("a", "b")), ImmutableMap.of("a", "b") },
                { ImmutableList.of(ImmutableMap.of("a", "b"), ImmutableMap.of()), ImmutableMap.of("a", "b") },
                { ImmutableList.of(ImmutableMap.of("a", "b"), ImmutableMap.of("a", "b")), ImmutableMap.of("a", "b") },
                { ImmutableList.of(ImmutableMap.of("a", "b"), ImmutableMap.of("a", "b"), ImmutableMap.of("a", "b")),
                        ImmutableMap.of("a", "b") },
                { ImmutableList.of(ImmutableMap.of("a", "b"), ImmutableMap.of("c", "d")),
                        ImmutableMap.of("a", "b", "c", "d") },
                { ImmutableList.of(ImmutableMap.of("a", "b", "c", "d"), ImmutableMap.of("a", "b")),
                        ImmutableMap.of("a", "b", "c", "d") } };
    }

    @Test(dataProvider = "joinTestDataprovider")
    public void testJoinIterator(final ImmutableList<Map<Object, Object>> maps, final Map<?, ?> expected) {
        assertEquals(Maps2.join(maps.iterator()), expected);
    }

    @Test(dataProvider = "joinTestDataprovider")
    public void testJoinIterable(final ImmutableList<Map<Object, Object>> maps, final Map<?, ?> expected) {
        assertEquals(Maps2.join(maps), expected);
    }

    @Test(dataProvider = "joinTestDataprovider")
    @SuppressWarnings("unchecked")
    public void testJoinArray(final ImmutableList<Map<Object, Object>> maps, final Map<?, ?> expected) {
        assertEquals(Maps2.join(maps.toArray(new Map[0])), expected);
    }

    public void testFilterEntriesSetMultimap() {
        final Map<String, ? extends Serializable> actual =
                Maps2.filterEntries(ImmutableMap.of("a", 2, "b", 1), (k, v) -> v.equals(1));
        assertEquals(actual, ImmutableMap.of("b", 1));
    }
}
