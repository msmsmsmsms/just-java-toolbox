package de.justsoftware.toolbox.mybatis.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import com.google.common.base.Preconditions;

import de.justsoftware.toolbox.mybatis.SupportedJdbcDriver;

/**
 * {@link BaseTypeHandler} which act differently for the different jdbc drivers
 */
@ParametersAreNonnullByDefault
public abstract class AbstractDriverSpecificTypeHandler<T> extends BaseTypeHandler<T> {

    private static final ThreadLocal<SupportedJdbcDriver> HOLDER = new ThreadLocal<>();

    private final SupportedJdbcDriver _jdbcDriver;

    public AbstractDriverSpecificTypeHandler() {
        _jdbcDriver = Preconditions.checkNotNull(HOLDER.get());
    }

    public static void setJdbcDriver(@Nullable final SupportedJdbcDriver newDriver) {
        HOLDER.set(newDriver);
    }

    @Override
    public final void setNonNullParameter(final PreparedStatement ps, final int i, final T parameter, final JdbcType jdbcType)
        throws SQLException {
        switch (_jdbcDriver) {
            case ORACLE:
                setNonNullParameterOracle(ps, i, parameter, jdbcType);
                return;
            case POSTGRES:
                setNonNullParameterPostgres(ps, i, parameter, jdbcType);
                return;
        }
        throwUnsupported();
    }

    @Nonnull
    private T throwUnsupported() {
        throw new UnsupportedOperationException("bahavior for " + _jdbcDriver + " not defined");
    }

    @Override
    public final T getNullableResult(final ResultSet rs, final String columnName) throws SQLException {
        switch (_jdbcDriver) {
            case ORACLE:
                return getNullableResultOracle(rs, columnName);
            case POSTGRES:
                return getNullableResultPostgres(rs, columnName);
        }
        return throwUnsupported();
    }

    @Override
    public final T getNullableResult(final ResultSet rs, final int columnIndex) throws SQLException {
        switch (_jdbcDriver) {
            case ORACLE:
                return getNullableResultOracle(rs, columnIndex);
            case POSTGRES:
                return getNullableResultPostgres(rs, columnIndex);
        }
        return throwUnsupported();
    }

    @Override
    public final T getNullableResult(final CallableStatement cs, final int columnIndex) throws SQLException {
        switch (_jdbcDriver) {
            case ORACLE:
                return getNullableResultOracle(cs, columnIndex);
            case POSTGRES:
                return getNullableResultPostgres(cs, columnIndex);
        }
        return throwUnsupported();
    }

    protected abstract void setNonNullParameterPostgres(PreparedStatement ps, int i, T parameter, @Nullable JdbcType jdbcType)
        throws SQLException;

    protected abstract void setNonNullParameterOracle(PreparedStatement ps, int i, T parameter, @Nullable JdbcType jdbcType)
        throws SQLException;

    @CheckForNull
    protected abstract T getNullableResultOracle(ResultSet rs, String columnName) throws SQLException;

    @CheckForNull
    protected abstract T getNullableResultPostgres(ResultSet rs, String columnName) throws SQLException;

    @CheckForNull
    protected abstract T getNullableResultPostgres(ResultSet rs, int columnIndex) throws SQLException;

    @CheckForNull
    protected abstract T getNullableResultOracle(ResultSet rs, int columnIndex) throws SQLException;

    @CheckForNull
    protected abstract T getNullableResultPostgres(CallableStatement cs, int columnIndex) throws SQLException;

    @CheckForNull
    protected abstract T getNullableResultOracle(CallableStatement cs, int columnIndex) throws SQLException;

}
