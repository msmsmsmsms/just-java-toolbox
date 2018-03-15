package de.justsoftware.toolbox.mybatis.result;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import javax.annotation.ParametersAreNonnullByDefault;

import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;

import de.justsoftware.toolbox.mybatis.DAOSupport;

/**
 * a result handler which can be used once to split a result into chunks and call a consumer for each chunk. This should not
 * be used directly, use {@link DAOSupport#forAllChunked} instead.
 *
 * @param <T>
 *            result type
 */
@ParametersAreNonnullByDefault
public final class ChunkedResultHandler<T> implements ResultHandler<T> {

    private final ArrayList<T> _results;
    private final int _chunkSize;
    private final Consumer<List<T>> _consumer;

    private int _count = 0;

    public ChunkedResultHandler(final int chunkSize, final Consumer<List<T>> consumer) {
        _chunkSize = chunkSize;
        _consumer = consumer;
        _results = new ArrayList<>(chunkSize);
    }

    @Override
    public void handleResult(final ResultContext<? extends T> resultContext) {
        _results.add(resultContext.getResultObject());
        if (_results.size() >= _chunkSize) {
            consume();
        }
        _count++;
    }

    private void consume() {
        _consumer.accept(Collections.unmodifiableList(_results));
        _results.clear();
    }

    public int finish() {
        consume();
        return _count;
    }

    public int applyTo(final Consumer<ResultHandler<T>> method) {
        method.accept(this);
        return finish();
    }

}
