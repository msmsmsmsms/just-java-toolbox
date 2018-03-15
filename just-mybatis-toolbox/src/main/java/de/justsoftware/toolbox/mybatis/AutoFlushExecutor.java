package de.justsoftware.toolbox.mybatis;

import java.sql.SQLException;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.executor.BatchResult;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.transaction.Transaction;

/**
 * an {@link Executor} which delegates all calls to a provided Executor
 * 
 * it flushes every {@link #_limit} update statements
 */
@ParametersAreNonnullByDefault
public final class AutoFlushExecutor implements Executor {

    private final Executor _delegate;

    private final int _limit;

    private int _counter;

    public AutoFlushExecutor(final Executor delegate, final int limit) {
        _delegate = delegate;
        delegate.setExecutorWrapper(this);
        _limit = limit;
    }

    @Override
    public int update(final MappedStatement ms, final Object parameter) throws SQLException {
        _counter++;
        if (_counter > _limit) {
            flushStatements();
        }
        return _delegate.update(ms, parameter);
    }

    @Override
    public List<BatchResult> flushStatements() throws SQLException {
        _counter = 0;
        return _delegate.flushStatements();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public <E> List<E> query(final MappedStatement ms, final Object parameter, final RowBounds rowBounds,
            final ResultHandler resultHandler, final CacheKey cacheKey, final BoundSql boundSql)
        throws SQLException {
        return _delegate.query(ms, parameter, rowBounds, resultHandler, cacheKey, boundSql);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public <E> List<E> query(final MappedStatement ms, final Object parameter, final RowBounds rowBounds,
            final ResultHandler resultHandler)
        throws SQLException {
        return _delegate.query(ms, parameter, rowBounds, resultHandler);
    }

    @Override
    public void commit(final boolean required) throws SQLException {
        _delegate.commit(required);
    }

    @Override
    public void rollback(final boolean required) throws SQLException {
        _delegate.rollback(required);
    }

    @Override
    public CacheKey createCacheKey(final MappedStatement ms, final Object parameterObject, final RowBounds rowBounds,
            final BoundSql boundSql) {
        return _delegate.createCacheKey(ms, parameterObject, rowBounds, boundSql);
    }

    @Override
    public boolean isCached(final MappedStatement ms, final CacheKey key) {
        return _delegate.isCached(ms, key);
    }

    @Override
    public void clearLocalCache() {
        _delegate.clearLocalCache();
    }

    @Override
    public void deferLoad(final MappedStatement ms, final MetaObject resultObject, final String property, final CacheKey key,
            final Class<?> targetType) {
        _delegate.deferLoad(ms, resultObject, property, key, targetType);
    }

    @Override
    public Transaction getTransaction() {
        return _delegate.getTransaction();
    }

    @Override
    public void close(final boolean forceRollback) {
        _delegate.close(forceRollback);
    }

    @Override
    public boolean isClosed() {
        return _delegate.isClosed();
    }

    @Override
    public void setExecutorWrapper(final Executor executor) {
        _delegate.setExecutorWrapper(executor);
    }

    @Override
    public <E> Cursor<E> queryCursor(final MappedStatement ms, final Object parameter, final RowBounds rowBounds)
        throws SQLException {
        return _delegate.queryCursor(ms, parameter, rowBounds);
    }

}
