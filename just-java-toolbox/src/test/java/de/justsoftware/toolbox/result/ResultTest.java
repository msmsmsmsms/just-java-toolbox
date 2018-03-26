package de.justsoftware.toolbox.result;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.testng.Assert.*;

public class ResultTest {
    private static final IllegalStateException EXCEPTION1 = new IllegalStateException("exception1");
    private static final IllegalArgumentException EXCEPTION2 = new IllegalArgumentException("exception2");

    @Test
    public void testFrom() {
        assertEquals(
                Result.from(() -> "x"),
                Result.ok("x"));

        assertEquals(
                Result.from(() -> { throw EXCEPTION1; }),
                Result.err(EXCEPTION1));
    }
    @Test
    public void testGetWithOk() throws Throwable {
        assertEquals(Result.getOrThrow(Result.ok("x")), "x");
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testGetWithErr()  {
        Result.getOrThrow(Result.err(EXCEPTION1));
    }

    @Test
    public void testIsOk() {
        assertTrue(Result.ok("x").isOk());
        assertFalse(Result.err(EXCEPTION1).isOk());
    }

    @Test
    public void testIfOk() {
        @SuppressWarnings("unchecked")
        Consumer<String> fOk = Mockito.mock(Consumer.class);
        @SuppressWarnings("unchecked")
        Consumer<String> fErr = Mockito.mock(Consumer.class);

        Result.ok("x").ifOk(fOk);
        Result.<String, Exception>err(EXCEPTION1).ifOk(fErr);

        Mockito.verify(fOk).accept("x");
        Mockito.verifyNoMoreInteractions(fErr);
    }

    @Test
    public void testIsErr() {
        assertFalse(Result.ok("x").isErr());
        assertTrue(Result.err(EXCEPTION1).isErr());
    }

    @Test
    public void testIfErr() {
        @SuppressWarnings("unchecked")
        Consumer<Exception> fOk = Mockito.mock(Consumer.class);
        @SuppressWarnings("unchecked")
        Consumer<Exception> fErr = Mockito.mock(Consumer.class);

        Result.<String, Exception>ok("x").ifErr(fOk);
        Result.err(EXCEPTION1).ifErr(fErr);

        Mockito.verifyNoMoreInteractions(fOk);
        Mockito.verify(fErr).accept(EXCEPTION1);
    }

    @Test
    public void testGetOk() {
        assertEquals(
                Result.ok("x").getOk(),
                Optional.of("x"));
        assertEquals(
                Result.err(EXCEPTION1).getOk(),
                Optional.empty());
    }

    @Test
    public void testGetErr() {
        assertEquals(
                Result.ok("x").getErr(),
                Optional.empty());
        assertEquals(
                Result.err(EXCEPTION1).getErr(),
                Optional.of(EXCEPTION1));
    }

    @Test
    public void testMap() {
        Function<Object, String> str = o -> o.toString();

        assertEquals(
                Result.ok(1).map(str),
                Result.ok("1"));
        assertEquals(
                Result.<Integer, Exception>err(EXCEPTION1).map(str),
                Result.err(EXCEPTION1));
    }

    @Test
    public void testMapErr() {
        assertEquals(
                Result.<Integer, Exception>ok(1).mapErr(Exception::getMessage),
                Result.ok(1));
        assertEquals(
                Result.<Integer, Exception>err(EXCEPTION1).mapErr(Exception::getMessage),
                Result.err("exception1"));
    }

    @Test
    public void testAnd() {
        assertEquals(
                Result.ok(1).and(Result.ok("x")),
                Result.ok("x"));
        assertEquals(
                Result.err(EXCEPTION1).and(Result.ok("x")),
                Result.err(EXCEPTION1));
    }

    @Test
    public void testAndThen() {
        Function<Object, Result<String, Exception>> str = o -> Result.ok(o.toString());

        assertEquals(
                Result.ok(1).andThen(str),
                Result.ok("1"));
        assertEquals(
                Result.<Integer, Exception>err(EXCEPTION1).andThen(str),
                Result.err(EXCEPTION1));


        Function<Object, Result<String, Exception>> err = o -> Result.err(EXCEPTION2);

        assertEquals(
                Result.ok(1).andThen(err),
                Result.err(EXCEPTION2));
        assertEquals(
                Result.<Integer, Exception>err(EXCEPTION1).andThen(err),
                Result.err(EXCEPTION1));
    }

    @Test
    public void testOr() {
        assertEquals(
                Result.ok(1).or(Result.ok("x")),
                Result.ok(1));
        assertEquals(
                Result.err(EXCEPTION1).or(Result.ok("x")),
                Result.ok("x"));
    }

    @Test
    public void testOrElse() {
        Function<Exception, Result<String, Exception>> str = e -> Result.ok(e.getMessage());

        assertEquals(
                Result.<Integer, Exception>ok(1).orElse(str),
                Result.ok(1));
        assertEquals(
                Result.<Integer, Exception>err(EXCEPTION1).orElse(str),
                Result.ok("exception1"));


        Function<Object, Result<String, Exception>> err = o -> Result.err(EXCEPTION2);

        assertEquals(
                Result.ok(1).orElse(err),
                Result.ok(1));
        assertEquals(
                Result.<Integer, Exception>err(EXCEPTION1).orElse(err),
                Result.err(EXCEPTION2));
    }

}
