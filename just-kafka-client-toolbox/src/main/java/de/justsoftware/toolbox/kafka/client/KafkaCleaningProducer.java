package de.justsoftware.toolbox.kafka.client;

import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import org.apache.kafka.clients.producer.Producer;

import com.google.common.collect.ImmutableSet;

/**
 * Interface for {@link Producer}s which are able to clean up messages during a full refill.
 *
 * @author Jan Burkhardt (initial creation)
 */
@ParametersAreNonnullByDefault
public interface KafkaCleaningProducer<K, V> extends Producer<K, V> {

    /**
     * Mark the beginning of a refill and specify which topics should be cleaned.
     *
     * Use a try with resource block to close this producer state properly.
     */
    @Nonnull
    default KafkaCleaningProducerState<K> beginRefill(final String... topics) {
        return beginRefill(ImmutableSet.copyOf(topics));
    }

    /**
     * Mark the beginning of a refill and specify which topics should be cleaned.
     *
     * Use a try with resource block to close this producer state properly.
     */
    @Nonnull
    KafkaCleaningProducerState<K> beginRefill(Set<String> topics);

}
