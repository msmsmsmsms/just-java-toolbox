/*
 * (c) Copyright 2016 Just Software AG
 *
 * Created on 05.12.2016 by Jan Burkhardt
 *
 * This file contains unpublished, proprietary trade secret information of
 * Just Software AG. Use, transcription, duplication and
 * modification are strictly prohibited without prior written consent of
 * Just Software AG.
 */
package de.justsoftware.toolbox.function;

import java.util.LinkedList;
import java.util.Queue;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * a consumer which can be used to split a {@link BiConsumer} into 2 consumers
 * 
 * the {@link BiConsumer#accept} method is called if both consumers are called
 * 
 * @author Jan Burkhardt (initial creation)
 */
@ParametersAreNonnullByDefault
public class SplitConsumer<L, R> {

    private final BiConsumer<L, R> _delegate;

    private final Queue<L> _leftQueue = new LinkedList<>();
    private final Queue<R> _rightQueue = new LinkedList<>();

    public final Consumer<L> _left = l -> accept(_leftQueue, l);
    public final Consumer<R> _right = r -> accept(_rightQueue, r);

    public SplitConsumer(final BiConsumer<L, R> delegate) {
        _delegate = delegate;
    }

    private <T> void accept(final Queue<T> list, final T value) {
        list.add(value);
        if (!_leftQueue.isEmpty() && !_rightQueue.isEmpty()) {
            _delegate.accept(_leftQueue.remove(), _rightQueue.remove());
        }
    }

    @Nonnull
    public Consumer<L> left() {
        return _left;
    }

    @Nonnull
    public Consumer<R> right() {
        return _right;
    }

}
