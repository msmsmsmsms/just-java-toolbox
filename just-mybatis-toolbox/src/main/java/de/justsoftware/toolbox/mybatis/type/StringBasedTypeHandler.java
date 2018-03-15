package de.justsoftware.toolbox.mybatis.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Function;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;

import com.google.common.base.Strings;

/**
 * type handler for string based ids
 */
@ParametersAreNonnullByDefault
@MappedJdbcTypes(value = { JdbcType.VARCHAR, JdbcType.CHAR, JdbcType.LONGVARCHAR, JdbcType.NVARCHAR, JdbcType.NCHAR },
        includeNullJdbcType = true)
public class StringBasedTypeHandler<T> extends BaseTypeHandler<T> {

    private final Function<T, String> _toString;
    private final Function<String, T> _fromString;

    public StringBasedTypeHandler(final Class<T> clz, final Function<T, String> toString) {
        this(TypeHandlers.construct(clz, String.class), toString);
    }

    public StringBasedTypeHandler(final Function<String, T> fromString, final Function<T, String> toString) {
        _fromString = fromString;
        _toString = toString;
    }

    @Override
    public final void setNonNullParameter(final PreparedStatement ps, final int i, final T parameter, final JdbcType jdbcType)
        throws SQLException {
        ps.setString(i, _toString.apply(parameter));
    }

    @Override
    public final T getNullableResult(final ResultSet rs, final String columnName) throws SQLException {
        return convert(rs.getString(columnName));
    }

    @CheckForNull
    private T convert(@Nullable final String s) {
        if (Strings.isNullOrEmpty(s)) {
            return null;
        }
        return _fromString.apply(s);
    }

    @Override
    public final T getNullableResult(final ResultSet rs, final int columnIndex) throws SQLException {
        return convert(rs.getString(columnIndex));
    }

    @Override
    public final T getNullableResult(final CallableStatement cs, final int columnIndex) throws SQLException {
        return convert(cs.getString(columnIndex));
    }

}
