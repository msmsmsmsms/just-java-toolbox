package de.justsoftware.toolbox.mybatis.result;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * procedure to execute a query with a list of parameters
 * 
 * @param <ID>
 *            the type of the parameters
 */
@ParametersAreNonnullByDefault
public interface NoResultQuery<ID> {

    /**
     * implement your query for a single partition here
     */
    void query(List<ID> ids);

}
