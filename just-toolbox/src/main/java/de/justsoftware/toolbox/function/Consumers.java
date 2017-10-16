/*
 * (c) Copyright 2016 Just Software AG
 *
 * Created on 28.11.2016 by Jan Burkhardt
 *
 * This file contains unpublished, proprietary trade secret information of
 * Just Software AG. Use, transcription, duplication and
 * modification are strictly prohibited without prior written consent of
 * Just Software AG.
 */
package de.justsoftware.toolbox.function;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * utilities for {@link Consumer}s
 * 
 * @author Jan Burkhardt (initial creation)
 */
@ParametersAreNonnullByDefault
public class Consumers {

    /**
     * a wrapped consumer which consumes only the first element
     */
    @Nonnull
    public static <V> Consumer<V> oneTime(final Consumer<V> consumer) {
        return new Consumer<V>() {

            private final AtomicBoolean _notYetConsumed = new AtomicBoolean(true);

            @Override
            public void accept(final V v) {
                if (_notYetConsumed.getAndSet(false)) {
                    consumer.accept(v);
                }
            }
        };
    }

    /**
     * a wrapped consumer which consumes only the first element
     */
    @Nonnull
    public static <T, U> BiConsumer<T, U> oneTime(final BiConsumer<T, U> consumer) {
        return new BiConsumer<T, U>() {

            private final AtomicBoolean _notYetConsumed = new AtomicBoolean(true);

            @Override
            public void accept(final T t, final U u) {
                if (_notYetConsumed.getAndSet(false)) {
                    consumer.accept(t, u);
                }
            }
        };
    }

    /**
     * Returns a consumer that just ignores the accepted argument
     */
    @Nonnull
    public static <T> Consumer<T> noOp() {
        return (arg) -> {
            //nothing to to
        };
    }

}
