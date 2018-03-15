package de.justsoftware.toolbox.mybatis.type.common;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import de.justsoftware.toolbox.mybatis.type.AbstractDriverSpecificTypeHandler;

/**
 * type handler for booleans which uses 't' and 'f' in oracle instead of 0 and 1 to have equivalent SQL
 */
@MappedTypes({ boolean.class, Boolean.class })
@MappedJdbcTypes(value = { JdbcType.BOOLEAN, JdbcType.BIT, JdbcType.CHAR }, includeNullJdbcType = true)
public class BooleanTypeHandler extends AbstractDriverSpecificTypeHandler<Boolean> {

    private static final String FALSE = "f";
    private static final String TRUE = "t";

    @Override
    protected void setNonNullParameterPostgres(final PreparedStatement ps, final int i, final Boolean parameter,
            final JdbcType jdbcType)
        throws SQLException {
        ps.setBoolean(i, parameter.booleanValue());
    }

    @Override
    protected void setNonNullParameterOracle(final PreparedStatement ps, final int i, final Boolean parameter,
            final JdbcType jdbcType)
        throws SQLException {
        ps.setString(i, parameter.booleanValue()
            ? TRUE
            : FALSE);
    }

    @Override
    protected Boolean getNullableResultPostgres(final ResultSet rs, final String columnName) throws SQLException {
        return getNullableResultPostgres(rs.getBoolean(columnName));
    }

    @Nonnull
    private static Boolean getNullableResultPostgres(final boolean v) {
        return Boolean.valueOf(v);
    }

    @Override
    protected Boolean getNullableResultPostgres(final ResultSet rs, final int columnIndex) throws SQLException {
        return getNullableResultPostgres(rs.getBoolean(columnIndex));
    }

    @Override
    protected Boolean getNullableResultPostgres(final CallableStatement cs, final int columnIndex) throws SQLException {
        return getNullableResultPostgres(cs.getBoolean(columnIndex));
    }

    @Override
    protected Boolean getNullableResultOracle(final ResultSet rs, final int columnIndex) throws SQLException {
        return getNullableResultOracle(rs.getString(columnIndex));
    }

    @Override
    protected Boolean getNullableResultOracle(final ResultSet rs, final String columnName) throws SQLException {
        return getNullableResultOracle(rs.getString(columnName));
    }

    @Override
    protected Boolean getNullableResultOracle(final CallableStatement cs, final int columnIndex) throws SQLException {
        return getNullableResultOracle(cs.getString(columnIndex));
    }

    @CheckForNull
    private Boolean getNullableResultOracle(@Nullable final String s) {
        if (s == null) {
            return null;
        } else if (TRUE.equals(s)) {
            return Boolean.TRUE;
        } else if (FALSE.equals(s)) {
            return Boolean.FALSE;
        } else {
            throw new IllegalStateException("don't know how to handle " + s);
        }
    }

}
