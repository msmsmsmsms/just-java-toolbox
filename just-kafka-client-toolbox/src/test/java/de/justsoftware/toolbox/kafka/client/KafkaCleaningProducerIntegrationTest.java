package de.justsoftware.toolbox.kafka.client;

import java.util.Properties;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

/**
 * integration test for {@link KafkaCleaningProducerImpl}
 *
 * this was used to implement the producer and can be used to find bugs
 *
 * @author Jan Burkhardt (initial creation)
 */
@ParametersAreNonnullByDefault
@SuppressWarnings("boxing")
public class KafkaCleaningProducerIntegrationTest {

    private static final String TOPIC = "KafkaCleaningProducerTest";
    private static final String BOOTSTRAP_SERVERS = "localhost:9092";

    private KafkaCleaningProducerImpl<String, String> _cleaner;

    @Nonnull
    private Properties producerConfig() {
        final Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 0);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        return props;
    }

    @Nonnull
    private Properties consumerConfig() {
        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 1000);
        return props;
    }

    /**
     * This method does not test anything drirectly, instead it fills kafka with messages which has to be investigated
     * manually.
     * Disabled because a running kafka instance is needed
     */
    public void generateDataAndExecuteRefill() {

        final Producer<String, String> producer =
                new KafkaProducer<>(producerConfig(), new StringSerializer(), new StringSerializer());

        _cleaner = new KafkaCleaningProducerImpl<>(producer,
                tp -> KafkaCleaningProducerImpl.createStringConsumer(tp, consumerConfig()));

        for (int i = 0; i < 2000; i++) {
            send(Integer.toString(i));
        }
        clear("10");

        _cleaner.flush();

        try (final KafkaCleaningProducerState<String> state = _cleaner.beginRefill(TOPIC)) {

            send("3");
            send("4");

            state.finishRefill();
        }

        _cleaner.close();
    }

    private void send(final String s) {
        _cleaner.send(new ProducerRecord<>(TOPIC, s, s));
    }

    private void clear(final String s) {
        _cleaner.send(new ProducerRecord<>(TOPIC, s, null));
    }

}
