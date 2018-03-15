package de.justsoftware.toolbox.mybatis;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import org.apache.ibatis.session.ResultHandler;

import de.justsoftware.toolbox.mybatis.result.ResultHandlerBuilder;

/**
 * support for calling queries which need partitioning or further result transformations
 */
@ParametersAreNonnullByDefault
public interface DAOSupport {

    /**
     * use this method to build a partition query/insert/update or delete in the following form:
     * 
     * for SELECTs
     * partion(ids).byId().asMap().query(...);
     * 
     * for DELETE, INSERT, UPDATEs
     * partion(ids).noResult(...);
     * 
     * @param <ID>
     *            type which is used to select
     */
    @Nonnull
    <ID> ResultHandlerBuilder<ID> partition(Set<? extends ID> ids);

    /**
     * instead of loading all results into memory this method can be used to split them into chunks and handle each chunk
     * 
     * @param chunkSize
     *            number of results which should be passed at the same time to consumer
     * @param consumer
     *            each chunk will be passed to this consumer
     * @param method
     *            a consumer which accepts a result handler and executes the database query, normally a method reference to a
     *            void returning method with a single ResultHandler parameter
     */
    <T> int forAllChunked(int chunkSize, Consumer<List<T>> consumer, Consumer<ResultHandler<T>> method);

    /**
     * instead of loading all results into memory this method can be used to handle each result
     *
     * @param consumer
     *            each result will be passed to this consumer
     * @param method
     *            a consumer which accepts a result handler and executes the database query, normally a method reference to a
     *            void returning method with a single ResultHandler parameter
     */
    <T> int forAll(Consumer<T> consumer, Consumer<ResultHandler<T>> method);

}
