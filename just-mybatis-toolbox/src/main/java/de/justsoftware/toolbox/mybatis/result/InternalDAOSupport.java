package de.justsoftware.toolbox.mybatis.result;

import java.util.Set;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * this part of the dao support is for internal usage only! (Don't use it in DAOs)
 */
@ParametersAreNonnullByDefault
public interface InternalDAOSupport {

    /**
     * this method splits the provided set into partitions which will fit into the maximum allowed size of IN queries and
     * calls the query method provided by the query, the result is handled by a result handler
     * 
     * @param <ID>
     *            type which is used usually in a WHERE IN clause, has to implement {@link #equals} and {@link #hashCode}
     *            correctly
     */
    <ID> void partition(Set<? extends ID> ids, NoResultQuery<ID> query);
}
