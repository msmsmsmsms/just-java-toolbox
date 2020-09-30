package de.justsoftware.toolbox.mybatis.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.LongFunction;
import java.util.function.ToLongFunction;

import javax.annotation.ParametersAreNonnullByDefault;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;

/**
 * {@link org.apache.ibatis.type.TypeHandler} for long based Ids.
 */
@ParametersAreNonnullByDefault
@MappedJdbcTypes(value = JdbcType.BIGINT, includeNullJdbcType = true)
public class LongBasedTypeHandler<ID> extends BaseTypeHandler<ID> {

    private final ToLongFunction<ID> _toLong;
    private final LongFunction<ID> _fromLong;

    public LongBasedTypeHandler(final Class<ID> clz, final ToLongFunction<ID> toLong) {
        this(TypeHandlers.constructLong(clz), toLong);
    }

    public LongBasedTypeHandler(final LongFunction<ID> fromLong, final ToLongFunction<ID> toLong) {
        _fromLong = fromLong;
        _toLong = toLong;
    }

    @Override
    public void setNonNullParameter(final PreparedStatement ps, final int i, final ID parameter, final JdbcType jdbcType)
        throws SQLException {
        ps.setLong(i, _toLong.applyAsLong(parameter));
    }

    @Override
    public ID getNullableResult(final ResultSet rs, final String columnName) throws SQLException {
        return getNullableResult(rs.getLong(columnName), rs.wasNull());
    }

    @Override
    public ID getNullableResult(final ResultSet rs, final int columnIndex) throws SQLException {
        return getNullableResult(rs.getLong(columnIndex), rs.wasNull());
    }

    @Override
    public ID getNullableResult(final CallableStatement cs, final int columnIndex) throws SQLException {
        return getNullableResult(cs.getLong(columnIndex), cs.wasNull());
    }

    /**
     * As we use {@link ResultSet#getLong} and {@link CallableStatement#getLong} we retrieve 0 if the
     * value in DB was null. Therefore {@link ResultSet#wasNull()} and {@link CallableStatement#wasNull()} have to be
     * used to determine a null value.
     * @param value the value retrieved by  {@link ResultSet#getLong} or {@link CallableStatement#getLong}
     * @param wasNull the value retrieved by {@link ResultSet#wasNull()} or {@link CallableStatement#wasNull()}
     */
   private ID getNullableResult(long value, boolean wasNull) {
        return wasNull ? null : _fromLong.apply(value);
   }

}
