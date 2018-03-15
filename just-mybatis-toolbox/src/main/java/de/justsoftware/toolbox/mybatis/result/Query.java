package de.justsoftware.toolbox.mybatis.result;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import org.apache.ibatis.session.ResultHandler;

/**
 * procedure to execute a query with a result handler and a list of parameters
 *
 * @param <ID>
 *            the type of the parameters
 * @param <RESULT>
 *            the result type of each returned row
 */
@ParametersAreNonnullByDefault
@FunctionalInterface
public interface Query<ID, RESULT> {

    /**
     * implement your query for a single partition here
     */
    void query(ResultHandler<RESULT> resultHandler, List<ID> ids);

}
