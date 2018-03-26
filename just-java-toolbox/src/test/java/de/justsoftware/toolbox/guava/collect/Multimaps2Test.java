package de.justsoftware.toolbox.guava.collect;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;

@Test
public class Multimaps2Test {

    public void testFilterEntriesMultimap() {
        final Multimap<String, Integer> result =
                Multimaps2.filterEntries(ImmutableListMultimap.of("a", 1, "a", 2, "b", 1), (k, v) -> v.equals(1));

        assertFalse(result instanceof SetMultimap);
        assertFalse(result instanceof ListMultimap);

        assertEquals(ImmutableListMultimap.copyOf(result), ImmutableListMultimap.of("a", 1, "b", 1));
    }

    public void testFilterEntriesSetMultimap() {
        assertEquals(
                Multimaps2.filterEntries(ImmutableSetMultimap.of("a", 1, "a", 2, "b", 1), (k, v) -> v.equals(2)),
                ImmutableSetMultimap.of("a", 2));
    }

}
