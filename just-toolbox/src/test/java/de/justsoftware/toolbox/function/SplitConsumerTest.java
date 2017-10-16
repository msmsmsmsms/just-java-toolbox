/*
 * (c) Copyright 2016 Just Software AG
 *
 * Created on 07.12.2016 by Jan Burkhardt
 *
 * This file contains unpublished, proprietary trade secret information of
 * Just Software AG. Use, transcription, duplication and
 * modification are strictly prohibited without prior written consent of
 * Just Software AG.
 */
package de.justsoftware.toolbox.function;

import static org.testng.Assert.assertEquals;

import java.util.LinkedList;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;

/**
 * test for {@link SplitConsumer}
 * 
 * @author Jan Burkhardt (initial creation)
 */
@Test
public class SplitConsumerTest {

    public void splitConsumerShouldForwardConsumedResults() {
        final LinkedList<String> lresult = new LinkedList<>();
        final LinkedList<String> rresult = new LinkedList<>();

        final SplitConsumer<String, String> c = new SplitConsumer<>((l, r) -> {
            lresult.add(l);
            rresult.add(r);
        });

        assertEquals(lresult, ImmutableList.of());
        assertEquals(rresult, ImmutableList.of());
        c._left.accept("l1");
        assertEquals(lresult, ImmutableList.of());
        assertEquals(rresult, ImmutableList.of());
        c._right.accept("r1");
        assertEquals(lresult, ImmutableList.of("l1"));
        assertEquals(rresult, ImmutableList.of("r1"));
        c._left.accept("l2");
        assertEquals(lresult, ImmutableList.of("l1"));
        assertEquals(rresult, ImmutableList.of("r1"));
        c._left.accept("l3");
        assertEquals(lresult, ImmutableList.of("l1"));
        assertEquals(rresult, ImmutableList.of("r1"));
        c._right.accept("r2");
        assertEquals(lresult, ImmutableList.of("l1", "l2"));
        assertEquals(rresult, ImmutableList.of("r1", "r2"));
    }

}
