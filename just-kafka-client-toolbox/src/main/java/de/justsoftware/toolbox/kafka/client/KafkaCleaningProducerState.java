package de.justsoftware.toolbox.kafka.client;

import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.annotation.ParametersAreNonnullByDefault;

import com.google.common.collect.ImmutableSet;

/**
 * State of a {@link KafkaCleaningProducer}.
 *
 * @author Jan Burkhardt (initial creation)
 */
@ParametersAreNonnullByDefault
public abstract class KafkaCleaningProducerState<K> implements AutoCloseable {

    protected final ImmutableSet<String> _topics;

    protected KafkaCleaningProducerState(final Set<String> topics) {
        _topics = ImmutableSet.copyOf(topics);
    }

    /**
     * Start clean up of events and unregister.
     */
    public final void finishRefill() {
        finishRefillTopicFilter(topic -> key -> true);
    }

    /**
     * Start clean up of events which match the given key predicate and unregister.
     *
     * @param keyPredicate
     *            pay attention only to keys which satisfy this predicate
     */
    public final void finishRefill(final Predicate<? super K> keyPredicate) {
        finishRefillTopicFilter(topic -> keyPredicate);
    }

    /**
     * Start clean up of events which match the given topic and key predicate and unregister.
     *
     * @param topicAndKeyPredicate
     *            pay attention only to key and topic tuples which satisfy this predicate, see {@link #finishRefill()} how to
     *            write the predicate
     */
    public abstract void finishRefillTopicFilter(Function<? super String, Predicate<? super K>> topicAndKeyPredicate);

    /**
     * {@inheritDoc}
     *
     * Implementations must not throw an exception on close and must support calling close multiple times.
     */
    @Override
    public abstract void close();

}
