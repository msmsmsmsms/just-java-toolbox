package de.justsoftware.toolbox.mybatis.result;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.ParametersAreNonnullByDefault;

import org.apache.ibatis.session.ResultHandler;

import com.google.common.collect.Iterables;

import de.justsoftware.toolbox.mybatis.DAOSupport;
import de.justsoftware.toolbox.mybatis.SupportedJdbcDriver;

/**
 * implementation of {@link DAOSupport}
 */
@ParametersAreNonnullByDefault
public class DAOSupportImpl implements DAOSupport, InternalDAOSupport {

    private final Supplier<SupportedJdbcDriver> _jdbcDriverSupplier;

    public DAOSupportImpl(final Supplier<SupportedJdbcDriver> jdbcDriverSupplier) {
        _jdbcDriverSupplier = jdbcDriverSupplier;
    }

    @Override
    public int defaultPartitionSize() {
        final SupportedJdbcDriver driver = _jdbcDriverSupplier.get();
        switch (driver) {
            case ORACLE:
                return ORACLE_SAVE_PARTITION_SIZE;
            case POSTGRES:
                return POSTGRES_SAVE_PARTITION_SIZE;
        }
        throw new UnsupportedOperationException("no behavior for " + driver + " defined");
    }

    @Override
    public <ID> ResultHandlerBuilder<ID> partition(final Set<? extends ID> ids) {
        return new ResultHandlerBuilder<>(this, ids);
    }

    @Override
    public <ID> void partition(final Set<? extends ID> ids, final NoResultQuery<ID> query) {
        for (final List<? extends ID> partition : Iterables.partition(ids, defaultPartitionSize())) {
            // view is wrapped to remove ? extends
            query.query(Collections.unmodifiableList(partition));
        }
    }

    @Override
    public <T> int forAllChunked(final int chunkSize, final Consumer<List<T>> consumer,
            final Consumer<ResultHandler<T>> method) {
        return new ChunkedResultHandler<>(chunkSize, consumer).applyTo(method);
    }

    @Override
    public <T> int forAll(final Consumer<T> consumer, final Consumer<ResultHandler<T>> method) {
        return new CountingResultHandler<>(consumer).applyTo(method);
    }

}
