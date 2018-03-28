package de.justsoftware.toolbox.stream;

import static com.google.common.collect.Maps.immutableEntry;
import static de.justsoftware.toolbox.stream.EntryStream.stream;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import static org.testng.AssertJUnit.assertFalse;

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

@ParametersAreNonnullByDefault
@Test
public class EntryStreamTest {

    public void testFromMap() {
        final ImmutableMap<String, String> map = ImmutableMap.of("key", "value");
        assertEquals(
                EntryStream.from(map)
                        .collect(EntryCollectors.toImmutableMap()),
                map);
    }

    public void testFromMultimap() {
        final ImmutableSetMultimap<String, String> map = ImmutableSetMultimap.of("key", "value");
        assertEquals(
                EntryStream.from(map)
                        .collect(EntryCollectors.toImmutableSetMultimap()),
                map);
    }

    public void testFromStream() {
        final ImmutableMap<String, String> map = ImmutableMap.of("key", "value");
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
                ImmutableMap.of("foo", "foobar"));
    }

    public void testFromKeyAndValueFunction() {
        assertEquals(
                EntryStream
                        .from(Stream.of("foo"), s -> s + "bar", s -> s + "baz")
                        .collect(EntryCollectors.toImmutableMap()),
                ImmutableMap.of("foobar", "foobaz"));
    }

    public void testDistinct() {
        assertEquals(stream("k", "v", "k", "v").count(), 2);
        assertEquals(stream("k", "v", "k", "v").distinct().count(), 1);
        assertEquals(stream("k", "v1", "k", "v2").distinct().count(), 2);
    }

    public void testPeek() {
        stream("k1", "v1", "k2", "v2")
                .peek((k, v) -> {
                    assertEquals(k, "k1");
                    assertEquals(v, "v1");
                });

        stream().peek((k, v) -> fail());
    }

    public void testSkip() {
        assertEquals(
                stream("k1", "v1", "k2", "v2")
                        .skip(1)
                        .collect(EntryCollectors.toImmutableMap()),
                ImmutableMap.of("k2", "v2"));
    }

    public void testLimit() {
        assertEquals(
                stream("k1", "v1", "k2", "v2")
                        .limit(1)
                        .collect(EntryCollectors.toImmutableMap()),
                ImmutableMap.of("k1", "v1"));
    }

    public void testFilterKey() {
        assertEquals(
                stream("k1", "v1", "k2", "v2")
                        .filterKey("k1"::equals)
                        .collect(EntryCollectors.toImmutableMap()),
                ImmutableMap.of("k1", "v1"));
    }

    public void testFilterValue() {
        assertEquals(
                stream("k1", "v1", "k2", "v2")
                        .filterValue("v1"::equals)
                        .collect(EntryCollectors.toImmutableMap()),
                ImmutableMap.of("k1", "v1"));
    }

    public void testFilter() {
        assertEquals(
                stream("k1", "v1", "k1", "v2", "k2", "v1")
                        .filter((k, v) -> "k1".equals(k) && "v1".equals(v))
                        .collect(EntryCollectors.toImmutableMap()),
                ImmutableMap.of("k1", "v1"));
    }

    public void testMapKey() {
        assertEquals(
                stream("k1", "v1", "k2", "v2")
                        .mapKey(k -> k + k)
                        .collect(EntryCollectors.toImmutableMap()),
                ImmutableMap.of("k1k1", "v1", "k2k2", "v2"));

    }

    public void testMapValue() {
        assertEquals(
                stream("k1", "v1", "k2", "v2")
                        .mapValue(v -> v + v)
                        .collect(EntryCollectors.toImmutableMap()),
                ImmutableMap.of("k1", "v1v1", "k2", "v2v2"));
    }

    public void testMap() {
        assertEquals(
                stream("k1", "v1", "k2", "v2")
                        .map((k, v) -> k + v)
                        .collect(ImmutableList.toImmutableList()),
                ImmutableList.of("k1v1", "k2v2"));
    }

    public void testMapToDouble() {
        assertEquals(
                stream("1.0", "1.0", "2.0", "2.0")
                        .mapToDouble((k, v) -> Double.parseDouble(k) * Double.parseDouble(v))
                        .toArray(),
                new double[] { 1, 4 });
    }

    public void testMapToInt() {
        assertEquals(
                stream("1", "1", "2", "2")
                        .mapToInt((k, v) -> Integer.parseInt(k) * Integer.parseInt(v))
                        .toArray(),
                new int[] { 1, 4 });
    }

    public void testMapToLong() {
        assertEquals(
                stream("1", "1", "2", "2")
                        .mapToLong((k, v) -> Long.parseLong(k) * Long.parseLong(v))
                        .toArray(),
                new long[] { 1, 4 });
    }

    public void testFlatMap() {
        assertEquals(
                stream("k1", "v1", "k2", "v2")
                        .flatMap((k, v) -> stream(k, v, k, k))
                        .collect(EntryCollectors.toImmutableSetMultimap()),
                ImmutableSetMultimap.of("k1", "k1", "k1", "v1", "k2", "k2", "k2", "v2"));
    }

    public void testFlatMapToObj() {
        assertEquals(
                stream("k1", "v1", "k2", "v2")
                        .flatMapToObj((k, v) -> Stream.of(k, v))
                        .collect(ImmutableList.toImmutableList()),
                ImmutableList.of("k1", "v1", "k2", "v2"));
    }

    public void testFlatMapToDouble() {
        assertEquals(
                stream("1", "2", "3", "4")
                        .flatMapToDouble((k, v) -> DoubleStream.of(Double.parseDouble(k), Double.parseDouble(v)))
                        .toArray(),
                new double[] { 1, 2, 3, 4 });
    }

    public void testFlatMapToInt() {
        assertEquals(
                stream("1", "2", "3", "4")
                        .flatMapToInt((k, v) -> IntStream.of(Integer.parseInt(k), Integer.parseInt(v)))
                        .toArray(),
                new int[] { 1, 2, 3, 4 });
    }

    public void testFlatMapToLong() {
        assertEquals(
                stream("1", "2", "3", "4")
                        .flatMapToLong((k, v) -> LongStream.of(Long.parseLong(k), Long.parseLong(v)))
                        .toArray(),
                new long[] { 1, 2, 3, 4 });
    }

    public void testSortedByKey() {
        assertEquals(
                stream("k3", "v3", "k1", "v1", "k2", "v2")
                        .sortedByKey(Comparator.naturalOrder()).keys()
                        .collect(ImmutableList.toImmutableList()),
                ImmutableList.of("k1", "k2", "k3"));
    }

    public void testSortedByValue() {
        assertEquals(
                stream("k3", "v3", "k1", "v1", "k2", "v2")
                        .sortedByValue(Comparator.naturalOrder()).values()
                        .collect(ImmutableList.toImmutableList()),
                ImmutableList.of("v1", "v2", "v3"));
    }

    public void testAllMatch() {
        assertTrue(stream("k1", "v1", "k2", "v2").allMatch((k, v) -> k.startsWith("k") && v.startsWith("v")));
        assertFalse(stream("k1", "v1", "k2", "v2").allMatch((k, v) -> "k1".equals(k) && "v1".equals(v)));
        assertFalse(stream("k1", "v1", "k2", "v2").allMatch((k, v) -> v.equals(k)));
    }

    public void testAnyMatch() {
        assertTrue(stream("k1", "v1", "k2", "v2").anyMatch((k, v) -> k.startsWith("k") && v.startsWith("v")));
        assertTrue(stream("k1", "v1", "k2", "v2").anyMatch((k, v) -> "k1".equals(k) && "v1".equals(v)));
        assertFalse(stream("k1", "v1", "k2", "v2").anyMatch((k, v) -> v.equals(k)));
    }

    public void testNoneMatch() {
        assertFalse(stream("k1", "v1", "k2", "v2").noneMatch((k, v) -> k.startsWith("k") && v.startsWith("v")));
        assertFalse(stream("k1", "v1", "k2", "v2").noneMatch((k, v) -> "k1".equals(k) && "v1".equals(v)));
        assertTrue(stream("k1", "v1", "k2", "v2").noneMatch((k, v) -> v.equals(k)));
    }

    public void testCount() {
        assertEquals(stream().count(), 0);
        assertEquals(stream("k1", "v1").count(), 1);
        assertEquals(stream("k1", "v1", "k2", "v2").count(), 2);
        assertEquals(stream("k1", "v1", "k2", "v2", "k3", "v3").count(), 3);
    }

    public void testKeys() {
        assertEquals(
                stream("k1", "v1", "k2", "v2", "k3", "v3")
                        .keys()
                        .collect(ImmutableList.toImmutableList()),
                ImmutableList.of("k1", "k2", "k3"));
    }

    public void testValues() {
        assertEquals(
                stream("k1", "v1", "k2", "v2", "k3", "v3")
                        .values()
                        .collect(ImmutableList.toImmutableList()),
                ImmutableList.of("v1", "v2", "v3"));
    }

    public void testMaxByKey() {
        assertEquals(
                stream("k3", "v", "k1", "v", "k2", "v")
                        .maxByKey(Comparator.naturalOrder()),
                Optional.of(immutableEntry("k3", "v")));
    }

    public void testMaxByValue() {
        assertEquals(
                stream("k", "v3", "k", "v1", "k", "v2")
                        .maxByValue(Comparator.naturalOrder()),
                Optional.of(immutableEntry("k", "v3")));
    }

    public void testMinByKey() {
        assertEquals(
                stream("k3", "v", "k1", "v", "k2", "v")
                        .minByKey(Comparator.naturalOrder()),
                Optional.of(immutableEntry("k1", "v")));
    }

    public void testMinByValue() {
        assertEquals(
                stream("k", "v3", "k", "v1", "k", "v2")
                        .minByValue(Comparator.naturalOrder()),
                Optional.of(immutableEntry("k", "v1")));
    }

    public void testForEach() {
        final ImmutableMap.Builder<String, String> actual = ImmutableMap.builder();

        stream("k1", "v1", "k2", "v2", "k3", "v3")
                .forEach(actual::put);

        assertEquals(actual.build(), ImmutableMap.of("k1", "v1", "k2", "v2", "k3", "v3"));
    }

    public void testForEachOrdered() {
        final ImmutableList.Builder<String> keys = ImmutableList.builder();

        stream("k1", "v1", "k2", "v2", "k3", "v3")
                .forEachOrdered((k, v) -> keys.add(k, v));

        assertEquals(keys.build(), ImmutableList.of("k1", "v1", "k2", "v2", "k3", "v3"));
    }

    public void testCollect() {
        assertEquals(
                stream("k1", "v1", "k2", "v2", "k3", "v3")
                        .collect(EntryCollectors.toImmutableMap()),
                ImmutableMap.of("k1", "v1", "k2", "v2", "k3", "v3"));
    }

}
