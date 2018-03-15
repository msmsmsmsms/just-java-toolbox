package de.justsoftware.toolbox.mybatis.result;

import java.util.function.Consumer;

import javax.annotation.ParametersAreNonnullByDefault;

import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;

/**
 * A {@link ResultHandler} which counts the invocations and forwards the result objects to a consumer.
 * 
 * @param <T>
 *            type of the handled object
 */
@ParametersAreNonnullByDefault
public final class CountingResultHandler<T> implements ResultHandler<T> {
    private final Consumer<T> _consumer;
    private int _count = 0;

    public CountingResultHandler(final Consumer<T> consumer) {
        _consumer = consumer;
    }

    @Override
    public void handleResult(final ResultContext<? extends T> resultContext) {
        _count++;
        _consumer.accept(resultContext.getResultObject());
    }

    public int applyTo(final Consumer<ResultHandler<T>> method) {
        method.accept(this);
        return _count;
    }

}
