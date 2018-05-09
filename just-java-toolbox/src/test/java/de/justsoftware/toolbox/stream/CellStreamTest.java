package de.justsoftware.toolbox.stream;

import static de.justsoftware.toolbox.guava.collect.ImmutableCollections.table;
import static de.justsoftware.toolbox.stream.CellCollectors.toImmutableTable;
import static de.justsoftware.toolbox.stream.CellStream.of;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

import javax.annotation.ParametersAreNonnullByDefault;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Tables;

@ParametersAreNonnullByDefault
@Test
public class CellStreamTest {

    public void testFromTable() {
        final ImmutableTable<String, String, String> table = table("r", "c", "v");
        assertEquals(
                CellStream.from(table)
                        .collect(toImmutableTable()),
                table);
    }

    public void testFromStream() {
        final ImmutableTable<String, String, String> table = table("r", "c", "v");
        assertEquals(
                CellStream.from(table.cellSet().stream())
                        .collect(toImmutableTable()),
                table);
    }

    public void testFromRowColumnAndValueFunction() {
        assertEquals(
                CellStream
                        .from(Stream.of("foo"), s -> s + "bar", s -> s + "baz", s -> s + "qux")
                        .collect(toImmutableTable()),
                table("foobar", "foobaz", "fooqux"));
    }

    public void testFromEntryStreamAndValueFunction() {
        assertEquals(
                CellStream
                        .from(EntryStream.of("foo", "bar"), (r, c) -> r + c)
                        .collect(toImmutableTable()),
                table("foo", "bar", "foobar"));
    }

    public void testDistinct() {
        assertEquals(CellStream.of("r", "c", "v", "r", "c", "v").count(), 2);
        assertEquals(CellStream.of("r", "c", "v", "r", "c", "v").distinct().count(), 1);
        assertEquals(CellStream.of("r", "c", "v", "r", "c", "v2").distinct().count(), 2);
    }

    public void testPeek() {
        CellStream.of("r1", "c1", "v1", "r2", "c2", "v2")
                .peek((r, c, v) -> {
                    assertEquals(r, "r1");
                    assertEquals(c, "c1");
                    assertEquals(v, "v1");
                });

        CellStream.of().peek((r, c, v) -> fail());
    }

    public void testSkip() {
        assertEquals(
                CellStream.of("r1", "c1", "v1", "r2", "c2", "v2")
                        .skip(1)
                        .collect(toImmutableTable()),
                table("r2", "c2", "v2"));
    }

    public void testLimit() {
        assertEquals(
                CellStream.of("r1", "c1", "v1", "r2", "c2", "v2")
                        .limit(1)
                        .collect(toImmutableTable()),
                table("r1", "c1", "v1"));
    }

    public void testFilterRow() {
        assertEquals(
                CellStream.of("r1", "c1", "v1", "r2", "c2", "v2")
                        .filterRow("r1"::equals)
                        .collect(toImmutableTable()),
                table("r1", "c1", "v1"));
    }

    public void testFilterColumn() {
        assertEquals(
                CellStream.of("r1", "c1", "v1", "r2", "c2", "v2")
                        .filterColumn("c1"::equals)
                        .collect(toImmutableTable()),
                table("r1", "c1", "v1"));
    }

    public void testFilterValue() {
        assertEquals(
                CellStream.of("r1", "c1", "v1", "r2", "c2", "v2")
                        .filterValue("v1"::equals)
                        .collect(toImmutableTable()),
                table("r1", "c1", "v1"));
    }

    public void testFilter() {
        assertEquals(
                of("r1", "c1", "v1", "r2", "c1", "v1", "r1", "c2", "v1", "r1", "c1", "v2")
                        .filter((r, c, v) -> "r1".equals(r) && "c1".equals(c) && "v1".equals(v))
                        .collect(toImmutableTable()),
                table("r1", "c1", "v1"));
    }

    public void testMapRow() {
        assertEquals(
                CellStream.of("r1", "c1", "v1", "r2", "c2", "v2")
                        .mapRow(r -> r + r)
                        .collect(toImmutableTable()),
                table("r1r1", "c1", "v1", "r2r2", "c2", "v2"));
    }

    public void testMapCol() {
        assertEquals(
                CellStream.of("r1", "c1", "v1", "r2", "c2", "v2")
                        .mapCol(c -> c + c)
                        .collect(toImmutableTable()),
                table("r1", "c1c1", "v1", "r2", "c2c2", "v2"));
    }

    public void testMapValue() {
        assertEquals(
                CellStream.of("r1", "c1", "v1", "r2", "c2", "v2")
                        .mapValue(v -> v + v)
                        .collect(toImmutableTable()),
                table("r1", "c1", "v1v1", "r2", "c2", "v2v2"));
    }

    public void testMap() {
        assertEquals(
                CellStream.of("r1", "c1", "v1", "r2", "c2", "v2")
                        .map((r, c, v) -> r + c + v)
                        .collect(ImmutableList.toImmutableList()),
                ImmutableList.of("r1c1v1", "r2c2v2"));
    }

    public void testFlatMap() {
        assertEquals(
                CellStream.of("r1", "c1", "v1", "r2", "c2", "v2")
                        .flatMap((r, c, v) -> CellStream.of(r, c, v, r, r, r))
                        .collect(toImmutableTable()),
                table("r1", "c1", "v1", "r2", "c2", "v2", "r1", "r1", "r1", "r2", "r2", "r2"));
    }

    public void testFlatMapToObj() {
        assertEquals(
                CellStream.of("r1", "c1", "v1", "r2", "c2", "v2")
                        .flatMapToObj((r, c, v) -> Stream.of(r, c, v))
                        .collect(ImmutableList.toImmutableList()),
                ImmutableList.of("r1", "c1", "v1", "r2", "c2", "v2"));
    }

    public void testSortedByRow() {
        assertEquals(
                CellStream.of("r3", "c3", "v3", "r1", "c1", "v1", "r2", "c2", "v2")
                        .sortedByRow(Comparator.naturalOrder())
                        .rows()
                        .collect(ImmutableList.toImmutableList()),
                ImmutableList.of("r1", "r2", "r3"));
    }

    public void testSortedByCol() {
        assertEquals(
                CellStream.of("r3", "c3", "v3", "r1", "c1", "v1", "r2", "c2", "v2")
                        .sortedByColumn(Comparator.naturalOrder())
                        .columns()
                        .collect(ImmutableList.toImmutableList()),
                ImmutableList.of("c1", "c2", "c3"));
    }

    public void testSortedByValue() {
        assertEquals(
                CellStream.of("r3", "c3", "v3", "r1", "c1", "v1", "r2", "c2", "v2")
                        .sortedByValue(Comparator.naturalOrder())
                        .values()
                        .collect(ImmutableList.toImmutableList()),
                ImmutableList.of("v1", "v2", "v3"));
    }

    public void testAllMatch() {
        assertTrue(CellStream.of("r1", "c1", "v1", "r2", "c2", "v2")
                .allMatch((r, c, v) -> r.startsWith("r") && c.startsWith("c") && v.startsWith("v")));
        assertFalse(CellStream.of("r1", "c1", "v1", "r2", "c2", "v2")
                .allMatch((r, c, v) -> "r1".equals(r) && "c1".equals(c) && "v1".equals(v)));
        assertFalse(CellStream.of("r1", "c1", "v1", "r2", "c2", "v2")
                .allMatch((r, c, v) -> v.equals(r)));
    }

    public void testAnyMatch() {
        assertTrue(CellStream.of("r1", "c1", "v1", "r2", "c2", "v2")
                .anyMatch((r, c, v) -> r.startsWith("r") && c.startsWith("c") && v.startsWith("v")));
        assertTrue(CellStream.of("r1", "c1", "v1", "r2", "c2", "v2")
                .anyMatch((r, c, v) -> "r1".equals(r) && "c1".equals(c) && "v1".equals(v)));
        assertFalse(CellStream.of("r1", "c1", "v1", "r2", "c2", "v2")
                .anyMatch((r, c, v) -> v.equals(r)));
    }

    public void testNoneMatch() {
        assertFalse(CellStream.of("r1", "c1", "v1", "r2", "c2", "v2")
                .noneMatch((r, c, v) -> r.startsWith("r") && c.startsWith("c") && v.startsWith("v")));
        assertFalse(CellStream.of("r1", "c1", "v1", "r2", "c2", "v2")
                .noneMatch((r, c, v) -> "r1".equals(r) && "c1".equals(c) && "v1".equals(v)));
        assertTrue(CellStream.of("r1", "c1", "v1", "r2", "c2", "v2")
                .noneMatch((r, c, v) -> v.equals(r)));
    }

    public void testCount() {
        assertEquals(CellStream.of().count(), 0);
        assertEquals(CellStream.of("r1", "c1", "v1").count(), 1);
        assertEquals(CellStream.of("r1", "c1", "v1", "r2", "c2", "v2").count(), 2);
        assertEquals(CellStream.of("r1", "c1", "v1", "r2", "c2", "v2", "r3", "c3", "v3").count(), 3);
    }

    public void testRows() {
        assertEquals(
                CellStream.of("r1", "c1", "v1", "r2", "c2", "v2", "r3", "c3", "v3")
                        .rows()
                        .collect(ImmutableList.toImmutableList()),
                ImmutableList.of("r1", "r2", "r3"));
    }

    public void testColumuns() {
        assertEquals(
                CellStream.of("r1", "c1", "v1", "r2", "c2", "v2", "r3", "c3", "v3")
                        .columns()
                        .collect(ImmutableList.toImmutableList()),
                ImmutableList.of("c1", "c2", "c3"));
    }

    public void testValues() {
        assertEquals(
                CellStream.of("r1", "c1", "v1", "r2", "c2", "v2", "r3", "c3", "v3")
                        .values()
                        .collect(ImmutableList.toImmutableList()),
                ImmutableList.of("v1", "v2", "v3"));
    }

    public void testMaxByKey() {
        assertEquals(
                CellStream.of("r3", "c", "v", "r1", "c", "v", "r2", "c", "v")
                        .maxByRow(Comparator.naturalOrder()),
                Optional.of(Tables.immutableCell("r3", "c", "v")));
    }

    public void testMaxByColumn() {
        assertEquals(
                CellStream.of("r", "c3", "v", "r", "c1", "v", "r", "c2", "v")
                        .maxByColumn(Comparator.naturalOrder()),
                Optional.of(Tables.immutableCell("r", "c3", "v")));
    }

    public void testMaxByValue() {
        assertEquals(
                CellStream.of("r", "c", "v3", "r", "c", "v1", "r", "c", "v2")
                        .maxByColumn(Comparator.naturalOrder()),
                Optional.of(Tables.immutableCell("r", "c", "v3")));
    }

    public void testMinByKey() {
        assertEquals(
                CellStream.of("r3", "c", "v", "r1", "c", "v", "r2", "c", "v")
                        .minByRow(Comparator.naturalOrder()),
                Optional.of(Tables.immutableCell("r1", "c", "v")));
    }

    public void testMinByColumn() {
        assertEquals(
                CellStream.of("r", "c3", "v", "r", "c1", "v", "r", "c2", "v")
                        .minByColumn(Comparator.naturalOrder()),
                Optional.of(Tables.immutableCell("r", "c1", "v")));
    }

    public void testMinByValue() {
        assertEquals(
                CellStream.of("r", "c", "v3", "r", "c", "v1", "r", "c", "v2")
                        .minByValue(Comparator.naturalOrder()),
                Optional.of(Tables.immutableCell("r", "c", "v1")));
    }

    public void testForEach() {
        final ImmutableTable.Builder<String, String, String> actual = ImmutableTable.builder();

        CellStream.of("r1", "c1", "v1", "r2", "c2", "v2", "r3", "c3", "v3")
                .forEach(actual::put);

        assertEquals(actual.build(), table("r1", "c1", "v1", "r2", "c2", "v2", "r3", "c3", "v3"));
    }

    public void testForEachOrdered() {
        final ImmutableList.Builder<String> keys = ImmutableList.builder();

        CellStream.of("r1", "c1", "v1", "r2", "c2", "v2", "r3", "c3", "v3")
                .forEachOrdered((r, c, v) -> keys.add(r, c, v));

        assertEquals(keys.build(), ImmutableList.of("r1", "c1", "v1", "r2", "c2", "v2", "r3", "c3", "v3"));
    }

    public void testCollect() {
        assertEquals(
                CellStream.of("r1", "c1", "v1", "r2", "c2", "v2", "r3", "c3", "v3")
                        .collect(toImmutableTable()),
                table("r1", "c1", "v1", "r2", "c2", "v2", "r3", "c3", "v3"));
    }

}
