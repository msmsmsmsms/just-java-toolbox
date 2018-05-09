package de.justsoftware.toolbox.kafka.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.MockConsumer;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.clients.producer.internals.DefaultPartitioner;
import org.apache.kafka.common.Cluster;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multiset;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

@RunWith(JUnitParamsRunner.class)
@SuppressWarnings("boxing")
@ParametersAreNonnullByDefault
public class KafkaCleaningProducerImplTest {

    private static final String TOPIC = "T";
    private static final StringSerializer STRING_SERIALIZER = new StringSerializer();
    private static final DefaultPartitioner DEFAULT_PARTITIONER = new DefaultPartitioner();

    @ParametersAreNonnullByDefault
    static class Mocks {

        private final class MockProducerExtension extends MockProducer<String, String> {

            private MockProducerExtension() {
                super(_cluster, true, DEFAULT_PARTITIONER, STRING_SERIALIZER, STRING_SERIALIZER);
            }

            @Override
            public synchronized Future<RecordMetadata> send(final ProducerRecord<String, String> record,
                    final Callback callback) {
                return super.send(record, new Callback() {
                    @Override
                    public void onCompletion(final RecordMetadata metadata, final Exception exception) {
                        assertNotNull(metadata);
                        final TopicPartition tp = new TopicPartition(metadata.topic(), metadata.partition());
                        final MockConsumer<String, String> consumer = consumerForPartion(tp);
                        consumer.addRecord(new ConsumerRecord<>(metadata.topic(), metadata.partition(),
                                metadata.offset(), record.key(), record.value()));

                        final ImmutableMap<TopicPartition, Long> endOffset =
                                ImmutableMap.of(tp, Math.max(_offsets.getOrDefault(tp, 0L), metadata.offset() + 1));
                        consumer.updateEndOffsets(endOffset);
                        _offsets.putAll(endOffset);

                        if (callback != null) {
                            callback.onCompletion(metadata, exception);
                        }
                    }
                });
            }

        }

        private final Cluster _cluster;

        private final ConcurrentMap<TopicPartition, Long> _offsets = new ConcurrentHashMap<>();

        private final MockProducer<String, String> _producer;

        private final Map<TopicPartition, MockConsumer<String, String>> _consumers = new HashMap<>();

        public Mocks(final Multiset<String> topics) {
            _cluster = new Cluster("test-cluster", ImmutableList.of(), partitions(topics), ImmutableSet.of(),
                    ImmutableSet.of());
            _producer = new MockProducerExtension();
        }

        @Nonnull
        private static List<PartitionInfo> partitions(final Multiset<String> topics) {
            final ImmutableList.Builder<PartitionInfo> result = ImmutableList.builder();
            for (final Multiset.Entry<String> e : topics.entrySet()) {
                for (int i = 0; i < e.getCount(); i++) {
                    result.add(new PartitionInfo(e.getElement(), i, null, null, null));
                }
            }
            return result.build();
        }

        @Nonnull
        private MockConsumer<String, String> consumerForPartion(final TopicPartition key) {
            return _consumers.computeIfAbsent(key, key1 -> {
                final MockConsumer<String, String> result =
                        new MockConsumer<>(OffsetResetStrategy.EARLIEST);
                result.assign(ImmutableList.of(key1));
                result.updateBeginningOffsets(ImmutableMap.of(key1, 0L));
                result.updateEndOffsets(ImmutableMap.of(key1, 0L));
                return result;
            });
        }

        @Nonnull
        public KafkaCleaningProducerImpl<String, String> cleaner() {
            return new KafkaCleaningProducerImpl<>(_producer, this::consumerForPartion);
        }

        @Nonnull
        private ImmutableList<ProducerRecord<String, String>> toRecords(
                final boolean partition, final String entries) {
            final ImmutableList.Builder<ProducerRecord<String, String>> result = ImmutableList.builder();
            for (final String s : Splitter.on(',').omitEmptyStrings().split(entries)) {
                final String key = s.substring(1);
                result.add(new ProducerRecord<>(TOPIC, partition
                    ? calculatePartition(TOPIC, key)
                    : null, key,
                        s.charAt(0) == 'i'
                            ? ""
                            : null));
            }
            return result.build();
        }

        private int calculatePartition(final String topic, final String s) {
            return DEFAULT_PARTITIONER.partition(topic, s, STRING_SERIALIZER.serialize(topic, s), null, null, _cluster);
        }

    }

    @Nonnull
    public Object[][] testcases() {
        return new Object[][] {
                { "i1", "", "d1" },
                { "", "", "" },
                { "i1", "i2", "d1" },
                { "", "i1", "" },
                { "i1", "i1", "" },
                { "", "d1", "" },
                { "d1", "d1", "" },
                { "i1", "d1", "" },
                { "i1,i2,i3,i4,d1", "i3,i4,i5", "d2" },
                { "ia,ib,ic,id,if,ig,ih,ij,ii,ik,il,im", "i3,i4,i5", "da,db,dc,dd,df,dg,dh,dj,di,dk,dl,dm" },
        };
    }

    @Test
    @Parameters(method = "testcases")
    public void testSinglePartition(final String queueBefore, final String fullIndex, final String expectedDelete) {
        test(ImmutableMultiset.of(TOPIC), queueBefore, fullIndex, expectedDelete);
    }

    @Test
    @Parameters(method = "testcases")
    public void testTwoPartitions(final String queueBefore, final String fullIndex, final String expectedDelete) {
        test(ImmutableMultiset.of(TOPIC, TOPIC), queueBefore, fullIndex, expectedDelete);
    }

    @Test
    @Parameters(method = "testcases")
    public void testFourPartitions(final String queueBefore, final String fullIndex, final String expectedDelete) {
        test(ImmutableMultiset.of(TOPIC, TOPIC, TOPIC, TOPIC), queueBefore, fullIndex, expectedDelete);
    }

    private void test(final ImmutableMultiset<String> topics, final String queueBefore, final String fullIndex,
            final String expectedDelete) {
        final Mocks m = new Mocks(topics);

        final ImmutableList<ProducerRecord<String, String>> recordsBefore = m.toRecords(false, queueBefore);
        for (final ProducerRecord<String, String> record : recordsBefore) {
            m._producer.send(record);
        }

        final KafkaCleaningProducerImpl<String, String> cleaner = m.cleaner();
        cleaner.setMaxSetSize(2);
        final ImmutableList<ProducerRecord<String, String>> recordsFullIndex;
        try (final KafkaCleaningProducerState<String> state = cleaner.beginRefill(topics.elementSet())) {

            recordsFullIndex = m.toRecords(false, fullIndex);
            for (final ProducerRecord<String, String> record : recordsFullIndex) {
                cleaner.send(record);
            }

            state.finishRefill();
        }

        final List<ProducerRecord<String, String>> history = m._producer.history();
        final ImmutableList<ProducerRecord<String, String>> expected = ImmutableList
                .<ProducerRecord<String, String>>builder()
                .addAll(recordsBefore)
                .addAll(recordsFullIndex)
                .build();

        assertEquals(history.subList(0, expected.size()), expected);

        final List<ProducerRecord<String, String>> remainingList = history.subList(expected.size(), history.size());
        final ImmutableSet<ProducerRecord<String, String>> remainingSet = ImmutableSet.copyOf(remainingList);
        assertEquals(remainingSet, ImmutableSet.copyOf(m.toRecords(true, expectedDelete)));
        assertEquals(remainingList.size(), remainingSet.size());
    }

    /**
     * disabled because IllegalStateException with "MockConsumer didn't have end offset specified, but tried to seek to end"
     * is thrown in {@link MockConsumer}
     */
    public void seekTest() {
        final MockConsumer<String, String> consumer = new MockConsumer<>(OffsetResetStrategy.EARLIEST);
        final TopicPartition tp = new TopicPartition("topic", 0);
        consumer.assign(ImmutableList.of(tp));
        consumer.addRecord(new ConsumerRecord<>(tp.topic(), tp.partition(), 0, "key", "value"));
        consumer.seekToEnd(ImmutableList.of(tp));
        assertEquals(consumer.position(tp), 1);
        consumer.close();
    }

    @Test
    public void testFilter() {
        final Mocks m = new Mocks(ImmutableMultiset.of(TOPIC));

        final ImmutableList<ProducerRecord<String, String>> recordsBefore =
                m.toRecords(false, "ia1,ib1,ia2,ib2,ia3,ib3,ia4,ib4");
        for (final ProducerRecord<String, String> record : recordsBefore) {
            m._producer.send(record);
        }

        final KafkaCleaningProducerImpl<String, String> cleaner = m.cleaner();
        cleaner.setMaxSetSize(2);
        final ImmutableList<ProducerRecord<String, String>> recordsFullIndex;
        try (final KafkaCleaningProducerState<String> state = cleaner.beginRefill(ImmutableSet.of(TOPIC))) {

            recordsFullIndex = m.toRecords(false, "ia2,ia4");
            for (final ProducerRecord<String, String> record : recordsFullIndex) {
                cleaner.send(record);
            }

            state.finishRefill(k -> k.startsWith("a"));
        }

        final List<ProducerRecord<String, String>> history = m._producer.history();
        final ImmutableList<ProducerRecord<String, String>> expected = ImmutableList
                .<ProducerRecord<String, String>>builder()
                .addAll(recordsBefore)
                .addAll(recordsFullIndex)
                .build();

        assertEquals(history.subList(0, expected.size()), expected);

        final List<ProducerRecord<String, String>> remainingList = history.subList(expected.size(), history.size());
        final ImmutableSet<ProducerRecord<String, String>> remainingSet = ImmutableSet.copyOf(remainingList);
        assertEquals(remainingSet, ImmutableSet.copyOf(m.toRecords(true, "da1,da3")));
        assertEquals(remainingList.size(), remainingSet.size());
    }

}
