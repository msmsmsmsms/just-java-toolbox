package de.justsoftware.toolbox.mybatis.type.common;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.annotation.ParametersAreNonnullByDefault;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeReference;

/**
 * this {@link TypeHandler} transforms empty business strings to null strings in database to emulate same behavior in postgres
 * as found in oracle
 */
@MappedJdbcTypes(value = { JdbcType.VARCHAR, JdbcType.CHAR, JdbcType.LONGVARCHAR, JdbcType.NVARCHAR, JdbcType.NCHAR },
        includeNullJdbcType = true)
@MappedTypes(String.class)
@ParametersAreNonnullByDefault
public class StringNullToEmptyTypeHandler extends TypeReference<String> implements TypeHandler<String> {

    @Override
    public void setParameter(final PreparedStatement ps, final int i, final String parameter, final JdbcType jdbcType)
        throws SQLException {
        if (parameter == null || parameter.isEmpty()) {
            ps.setNull(i, (jdbcType != null
                ? jdbcType
                : JdbcType.VARCHAR).TYPE_CODE);
        } else {
            ps.setString(i, parameter);
        }
    }

    @Override
    public String getResult(final ResultSet rs, final String columnName) throws SQLException {
        final String result = rs.getString(columnName);
        return rs.wasNull() || result == null
            ? ""
            : result;
    }

    @Override
    public String getResult(final ResultSet rs, final int columnIndex) throws SQLException {
        final String result = rs.getString(columnIndex);
        return rs.wasNull() || result == null
            ? ""
            : result;
    }

    @Override
    public String getResult(final CallableStatement cs, final int columnIndex) throws SQLException {
        final String result = cs.getString(columnIndex);
        return cs.wasNull() || result == null
            ? ""
            : result;
    }

}
