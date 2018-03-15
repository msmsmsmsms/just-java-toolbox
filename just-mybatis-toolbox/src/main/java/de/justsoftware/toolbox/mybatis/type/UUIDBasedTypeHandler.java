package de.justsoftware.toolbox.mybatis.type;

import java.nio.ByteBuffer;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.function.Function;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;

/**
 * {@link org.apache.ibatis.type.TypeHandler} for UUIDs
 */
@ParametersAreNonnullByDefault
@MappedJdbcTypes(value = { JdbcType.OTHER, JdbcType.BINARY, JdbcType.VARBINARY }, includeNullJdbcType = true)
public abstract class UUIDBasedTypeHandler<ID> extends AbstractDriverSpecificTypeHandler<ID> {

    private static final int UUID_BYTES = 16;

    private final Function<UUID, ID> _fromUUID;
    private final Function<ID, UUID> _toUUID;

    public UUIDBasedTypeHandler(final Class<ID> clz, final Function<ID, UUID> toUUID) {
        this(TypeHandlers.construct(clz, UUID.class), toUUID);
    }

    public UUIDBasedTypeHandler(final Function<UUID, ID> fromUUID, final Function<ID, UUID> toUUID) {
        _fromUUID = fromUUID;
        _toUUID = toUUID;
    }

    @Nonnull
    private static byte[] toByteArray(final UUID uuid) {
        return ByteBuffer.allocate(UUID_BYTES)
                .putLong(uuid.getMostSignificantBits())
                .putLong(uuid.getLeastSignificantBits())
                .array();
    }

    @CheckForNull
    private ID createIdNullable(@Nullable final UUID id) {
        return id != null
            ? _fromUUID.apply(id)
            : null;
    }

    @Override
    protected void setNonNullParameterPostgres(final PreparedStatement ps, final int i, final ID parameter,
            final JdbcType jdbcType)
        throws SQLException {
        final UUID uuid = _toUUID.apply(parameter);
        if (jdbcType != null) {
            ps.setObject(i, uuid, jdbcType.TYPE_CODE);
        } else {
            ps.setObject(i, uuid);
        }
    }

    @Override
    protected ID getNullableResultPostgres(final ResultSet rs, final String columnName) throws SQLException {
        return createIdNullable((UUID) rs.getObject(columnName));
    }

    @Override
    protected ID getNullableResultPostgres(final ResultSet rs, final int columnIndex) throws SQLException {
        return createIdNullable((UUID) rs.getObject(columnIndex));
    }

    @Override
    protected ID getNullableResultPostgres(final CallableStatement cs, final int columnIndex) throws SQLException {
        return createIdNullable((UUID) cs.getObject(columnIndex));
    }

    @CheckForNull
    private ID createIdNullable(@Nullable final byte[] obj) {
        if (obj == null || obj.length == 0) {
            return null;
        }
        final ByteBuffer buffer = ByteBuffer.wrap(obj);
        final long mostSignificantBits = buffer.getLong();
        final long leastSignificantBits = buffer.getLong();
        return createIdNullable(new UUID(mostSignificantBits, leastSignificantBits));
    }

    @Override
    protected void setNonNullParameterOracle(final PreparedStatement ps, final int i, final ID parameter,
            final JdbcType jdbcType)
        throws SQLException {
        ps.setBytes(i, toByteArray(_toUUID.apply(parameter)));
    }

    @Override
    protected ID getNullableResultOracle(final ResultSet rs, final String columnName) throws SQLException {
        return createIdNullable(rs.getBytes(columnName));
    }

    @Override
    protected ID getNullableResultOracle(final ResultSet rs, final int columnIndex) throws SQLException {
        return createIdNullable(rs.getBytes(columnIndex));
    }

    @Override
    protected ID getNullableResultOracle(final CallableStatement cs, final int columnIndex) throws SQLException {
        return createIdNullable(cs.getBytes(columnIndex));
    }

}
