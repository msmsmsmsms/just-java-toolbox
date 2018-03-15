package de.justsoftware.toolbox.mybatis.result;

import java.util.stream.Collector;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;

/**
 * Generic {@link ResultHandler} which wraps around a collector
 * 
 * @param <ID>
 *            the type of the handled ids in partition queries
 * @param <DB_RESULT>
 *            the type of the database result
 * @param <A>
 *            the type of the used builder, see {@link Collector}
 * @param <RESULT>
 *            the type of the collected result
 */
@ParametersAreNonnullByDefault
public final class CollectorResultHandler<ID, DB_RESULT, A, RESULT> implements ResultHandler<DB_RESULT> {

    private final ResultHandlerBuilder<ID> _resultHandlerBuilder;
    private final Collector<DB_RESULT, A, RESULT> _collector;
    private final A _a;

    CollectorResultHandler(final ResultHandlerBuilder<ID> resultHandlerBuilder,
            final Collector<DB_RESULT, A, RESULT> collector) {
        _resultHandlerBuilder = resultHandlerBuilder;
        _collector = collector;
        _a = collector.supplier().get();
    }

    @Override
    public void handleResult(final ResultContext<? extends DB_RESULT> resultContext) {
        _collector.accumulator().accept(_a, resultContext.getResultObject());
    }

    @Nonnull
    public final RESULT query(final Query<ID, DB_RESULT> query) {
        _resultHandlerBuilder.noResult(ids -> query.query(this, ids));
        return _collector.finisher().apply(_a);
    }
}
