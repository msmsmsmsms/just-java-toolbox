package de.justsoftware.toolbox.stream;

import static com.google.common.collect.Maps.immutableEntry;
import static de.justsoftware.toolbox.guava.collect.ImmutableCollections.list;
import static de.justsoftware.toolbox.guava.collect.ImmutableCollections.map;
import static de.justsoftware.toolbox.guava.collect.ImmutableCollections.setMultimap;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.Comparator;
import java.util.Optional;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import javax.annotation.ParametersAreNonnullByDefault;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSetMultimap;

import de.justsoftware.toolbox.guava.collect.ImmutableCollections;

@ParametersAreNonnullByDefault
@Test
public class EntryStreamTest {

    public void testFromMap() {
        final ImmutableMap<String, String> map = map("key", "value");
        assertEquals(
                EntryStream.from(map)
                        .collect(EntryCollectors.toImmutableMap()),
                map);
    }

    public void testFromMultimap() {
        final ImmutableSetMultimap<String, String> map = setMultimap("key", "value");
        assertEquals(
                EntryStream.from(map)
                        .collect(EntryCollectors.toImmutableSetMultimap()),
                map);
    }

    public void testFromStream() {
        final ImmutableMap<String, String> map = map("key", "value");
        assertEquals(
                EntryStream.from(map.entrySet().stream())
                        .collect(EntryCollectors.toImmutableMap()),
                map);
    }

    public void testFromValueFunction() {
        assertEquals(
                EntryStream
                        .from(Stream.of("foo"), s -> s + "bar")
                        .collect(EntryCollectors.toImmutableMap()),
                map("foo", "foobar"));
    }

    public void testFromKeyAndValueFunction() {
        assertEquals(
                EntryStream
                        .from(ImmutableCollections.stream("foo"), s -> s + "bar", s -> s + "baz")
                        .collect(EntryCollectors.toImmutableMap()),
                map("foobar", "foobaz"));
    }

    public void testDistinct() {
        assertEquals(EntryStream.of("k", "v", "k", "v").count(), 2);
        assertEquals(EntryStream.of("k", "v", "k", "v").distinct().count(), 1);
        assertEquals(EntryStream.of("k", "v1", "k", "v2").distinct().count(), 2);
    }

    public void testPeek() {
        EntryStream.of("k1", "v1", "k2", "v2")
                .peek((k, v) -> {
                    assertEquals(k, "k1");
                    assertEquals(v, "v1");
                });

        EntryStream.of().peek((k, v) -> fail());
    }

    public void testSkip() {
        assertEquals(
                EntryStream.of("k1", "v1", "k2", "v2")
                        .skip(1)
                        .collect(EntryCollectors.toImmutableMap()),
                map("k2", "v2"));
    }

    public void testLimit() {
        assertEquals(
                EntryStream.of("k1", "v1", "k2", "v2")
                        .limit(1)
                        .collect(EntryCollectors.toImmutableMap()),
                map("k1", "v1"));
    }

    public void testFilterKey() {
        assertEquals(
                EntryStream.of("k1", "v1", "k2", "v2")
                        .filterKey("k1"::equals)
                        .collect(EntryCollectors.toImmutableMap()),
                map("k1", "v1"));
    }

    public void testFilterValue() {
        assertEquals(
                EntryStream.of("k1", "v1", "k2", "v2")
                        .filterValue("v1"::equals)
                        .collect(EntryCollectors.toImmutableMap()),
                map("k1", "v1"));
    }

    public void testFilter() {
        assertEquals(
                EntryStream.of("k1", "v1", "k1", "v2", "k2", "v1")
                        .filter((k, v) -> "k1".equals(k) && "v1".equals(v))
                        .collect(EntryCollectors.toImmutableMap()),
                map("k1", "v1"));
    }

    public void testMapKey() {
        assertEquals(
                EntryStream.of("k1", "v1", "k2", "v2")
                        .mapKey(k -> k + k)
                        .collect(EntryCollectors.toImmutableMap()),
                map("k1k1", "v1", "k2k2", "v2"));

    }

    public void testMapValue() {
        assertEquals(
                EntryStream.of("k1", "v1", "k2", "v2")
                        .mapValue(v -> v + v)
                        .collect(EntryCollectors.toImmutableMap()),
                map("k1", "v1v1", "k2", "v2v2"));
    }

    public void testMap() {
        assertEquals(
                EntryStream.of("k1", "v1", "k2", "v2")
                        .map((k, v) -> k + v)
                        .collect(ImmutableList.toImmutableList()),
                list("k1v1", "k2v2"));
    }

    public void testMapToDouble() {
        assertEquals(
                EntryStream.of("1.0", "1.0", "2.0", "2.0")
                        .mapToDouble((k, v) -> Double.parseDouble(k) * Double.parseDouble(v))
                        .toArray(),
                new double[] { 1, 4 });
    }

    public void testMapToInt() {
        assertEquals(
                EntryStream.of("1", "1", "2", "2")
                        .mapToInt((k, v) -> Integer.parseInt(k) * Integer.parseInt(v))
                        .toArray(),
                new int[] { 1, 4 });
    }

    public void testMapToLong() {
        assertEquals(
                EntryStream.of("1", "1", "2", "2")
                        .mapToLong((k, v) -> Long.parseLong(k) * Long.parseLong(v))
                        .toArray(),
                new long[] { 1, 4 });
    }

    public void testFlatMap() {
        assertEquals(
                EntryStream.of("k1", "v1", "k2", "v2")
                        .flatMap((k, v) -> EntryStream.of(k, v, k, k))
                        .collect(EntryCollectors.toImmutableSetMultimap()),
                setMultimap("k1", "k1", "k1", "v1", "k2", "k2", "k2", "v2"));
    }

    public void testFlatMapToObj() {
        assertEquals(
                EntryStream.of("k1", "v1", "k2", "v2")
                        .flatMapToObj((k, v) -> Stream.of(k, v))
                        .collect(ImmutableList.toImmutableList()),
                list("k1", "v1", "k2", "v2"));
    }

    public void testFlatMapToDouble() {
        assertEquals(
                EntryStream.of("1", "2", "3", "4")
                        .flatMapToDouble((k, v) -> DoubleStream.of(Double.parseDouble(k), Double.parseDouble(v)))
                        .toArray(),
                new double[] { 1, 2, 3, 4 });
    }

    public void testFlatMapToInt() {
        assertEquals(
                EntryStream.of("1", "2", "3", "4")
                        .flatMapToInt((k, v) -> IntStream.of(Integer.parseInt(k), Integer.parseInt(v)))
                        .toArray(),
                new int[] { 1, 2, 3, 4 });
    }

    public void testFlatMapToLong() {
        assertEquals(
                EntryStream.of("1", "2", "3", "4")
                        .flatMapToLong((k, v) -> LongStream.of(Long.parseLong(k), Long.parseLong(v)))
                        .toArray(),
                new long[] { 1, 2, 3, 4 });
    }

    public void testSortedByKey() {
        assertEquals(
                EntryStream.of("k3", "v3", "k1", "v1", "k2", "v2")
                        .sortedByKey(Comparator.naturalOrder())
                        .keys()
                        .collect(ImmutableList.toImmutableList()),
                list("k1", "k2", "k3"));
    }

    public void testSortedByValue() {
        assertEquals(
                EntryStream.of("k3", "v3", "k1", "v1", "k2", "v2")
                        .sortedByValue(Comparator.naturalOrder())
                        .values()
                        .collect(ImmutableList.toImmutableList()),
                list("v1", "v2", "v3"));
    }

    public void testAllMatch() {
        assertTrue(EntryStream.of("k1", "v1", "k2", "v2").allMatch((k, v) -> k.startsWith("k") && v.startsWith("v")));
        assertFalse(EntryStream.of("k1", "v1", "k2", "v2").allMatch((k, v) -> "k1".equals(k) && "v1".equals(v)));
        assertFalse(EntryStream.of("k1", "v1", "k2", "v2").allMatch((k, v) -> v.equals(k)));
    }

    public void testAnyMatch() {
        assertTrue(EntryStream.of("k1", "v1", "k2", "v2").anyMatch((k, v) -> k.startsWith("k") && v.startsWith("v")));
        assertTrue(EntryStream.of("k1", "v1", "k2", "v2").anyMatch((k, v) -> "k1".equals(k) && "v1".equals(v)));
        assertFalse(EntryStream.of("k1", "v1", "k2", "v2").anyMatch((k, v) -> v.equals(k)));
    }

    public void testNoneMatch() {
        assertFalse(EntryStream.of("k1", "v1", "k2", "v2").noneMatch((k, v) -> k.startsWith("k") && v.startsWith("v")));
        assertFalse(EntryStream.of("k1", "v1", "k2", "v2").noneMatch((k, v) -> "k1".equals(k) && "v1".equals(v)));
        assertTrue(EntryStream.of("k1", "v1", "k2", "v2").noneMatch((k, v) -> v.equals(k)));
    }

    public void testCount() {
        assertEquals(EntryStream.of().count(), 0);
        assertEquals(EntryStream.of("k1", "v1").count(), 1);
        assertEquals(EntryStream.of("k1", "v1", "k2", "v2").count(), 2);
        assertEquals(EntryStream.of("k1", "v1", "k2", "v2", "k3", "v3").count(), 3);
    }

    public void testKeys() {
        assertEquals(
                EntryStream.of("k1", "v1", "k2", "v2", "k3", "v3")
                        .keys()
                        .collect(ImmutableList.toImmutableList()),
                list("k1", "k2", "k3"));
    }

    public void testValues() {
        assertEquals(
                EntryStream.of("k1", "v1", "k2", "v2", "k3", "v3")
                        .values()
                        .collect(ImmutableList.toImmutableList()),
                list("v1", "v2", "v3"));
    }

    public void testMaxByKey() {
        assertEquals(
                EntryStream.of("k3", "v", "k1", "v", "k2", "v")
                        .maxByKey(Comparator.naturalOrder()),
                Optional.of(immutableEntry("k3", "v")));
    }

    public void testMaxByValue() {
        assertEquals(
                EntryStream.of("k", "v3", "k", "v1", "k", "v2")
                        .maxByValue(Comparator.naturalOrder()),
                Optional.of(immutableEntry("k", "v3")));
    }

    public void testMinByKey() {
        assertEquals(
                EntryStream.of("k3", "v", "k1", "v", "k2", "v")
                        .minByKey(Comparator.naturalOrder()),
                Optional.of(immutableEntry("k1", "v")));
    }

    public void testMinByValue() {
        assertEquals(
                EntryStream.of("k", "v3", "k", "v1", "k", "v2")
                        .minByValue(Comparator.naturalOrder()),
                Optional.of(immutableEntry("k", "v1")));
    }

    public void testForEach() {
        final ImmutableMap.Builder<String, String> actual = ImmutableMap.builder();

        EntryStream.of("k1", "v1", "k2", "v2", "k3", "v3")
                .forEach(actual::put);

        assertEquals(actual.build(), map("k1", "v1", "k2", "v2", "k3", "v3"));
    }

    public void testForEachOrdered() {
        final ImmutableList.Builder<String> keys = ImmutableList.builder();

        EntryStream.of("k1", "v1", "k2", "v2", "k3", "v3")
                .forEachOrdered((k, v) -> keys.add(k, v));

        assertEquals(keys.build(), list("k1", "v1", "k2", "v2", "k3", "v3"));
    }

    public void testCollect() {
        assertEquals(
                EntryStream.of("k1", "v1", "k2", "v2", "k3", "v3")
                        .collect(EntryCollectors.toImmutableMap()),
                map("k1", "v1", "k2", "v2", "k3", "v3"));
    }

}
