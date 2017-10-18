package de.justsoftware.toolbox.concurrent;

import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import com.google.common.base.MoreObjects;
import com.google.common.util.concurrent.Runnables;

import de.justsoftware.toolbox.function.Consumers;

/**
 * this object creates a thread which runs the provided method until it is canceled or interrupted
 * 
 * @author Jan Burkhardt (initial creation)
 */
@ParametersAreNonnullByDefault
public final class RepeatingThread {

    private volatile Runnable _onFinish = Runnables.doNothing();
    private volatile Consumer<Throwable> _onException = Consumers.noOp();

    private ThreadDelegate _thread;

    public RepeatingThread(final String name, final Runnable task) {
        _thread = new ThreadDelegate(name, task);
        _thread.setUncaughtExceptionHandler(this::onException);
        _thread.start();
    }

    public void cancel() {
        if (_thread != null) {
            _thread.interrupt();
            _thread = null;
        }
    }

    @Nonnull
    public RepeatingThread setPriority(final int newPriority) {
        _thread.setPriority(newPriority);
        return this;
    }

    @Nonnull
    public RepeatingThread onFinish(@Nullable final Runnable onFinish) {
        _onFinish = MoreObjects.firstNonNull(onFinish, Runnables.doNothing());
        return this;
    }

    @Nonnull
    public RepeatingThread onException(@Nullable final Consumer<Throwable> onException) {
        _onException = MoreObjects.firstNonNull(onException, Consumers.noOp());
        return this;
    }

    /**
     * @param t
     *            is needed to fulfill UncaughtExceptionHandler interface
     */
    private void onException(@SuppressWarnings("unused") final Thread t, final Throwable e) {
        _onException.accept(e);
    }

    /**
     * this thread is designed that no reference to it is hold, so it can be removed immediately after it is
     * finished
     * 
     * @author Jan Burkhardt (initial creation)
     */
    @ParametersAreNonnullByDefault
    private final class ThreadDelegate extends Thread {
        private final Runnable _task;

        private ThreadDelegate(final String name, final Runnable task) {
            super(name);
            _task = task;
        }

        @Override
        public void run() {
            while (_thread == this && !isInterrupted()) {
                try {
                    _task.run();
                } catch (final RuntimeException e) {
                    onException(this, e);
                }
            }
            _onFinish.run();
        }

    }

}
