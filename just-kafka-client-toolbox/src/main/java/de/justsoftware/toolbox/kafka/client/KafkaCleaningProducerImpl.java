package de.justsoftware.toolbox.kafka.client;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.Metric;
import org.apache.kafka.common.MetricName;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.ProducerFencedException;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.StringDeserializer;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;

/**
 * Implementation of {@link KafkaCleaningProducer} which reads the topic partition by partition
 * and deletes all messages which keys were not sent to kafka and match the given cleanup predicate (if specified).
 *
 * To prevent too much memory consumption only a predefined amount of data is stored.
 * Instead the queue is read multiple times.
 *
 * The key has to implement {@link #equals} and {@link #hashCode} correctly.
 *
 * @author Jan Burkhardt (initial creation)
 */
@ParametersAreNonnullByDefault
public class KafkaCleaningProducerImpl<K, V> implements KafkaCleaningProducer<K, V> {

    private static final int DEFAULT_MAX_SIZE = 100000;

    /**
     * reusable callback
     */
    private final Callback _nullCallback = new RecognizeCallback(null);

    /**
     * the producer which is wrapped
     */
    private final Producer<K, V> _delegate;

    /**
     * an consumer is created for every partition and topic to be able to clean multiple partitions in multiple threads. the
     * consumer is created only once.
     */
    private final Function<TopicPartition, ? extends Consumer<K, ?>> _consumerCreator;

    /**
     * the maximum set size, when the set of deleted keys reaches this limit only entries in the range a read
     */
    private int _maxSetSize = DEFAULT_MAX_SIZE;

    private final SetMultimap<String, KafkaCleaningProducerImplState> _activeStates =
            Multimaps.synchronizedSetMultimap(HashMultimap.create());

    public KafkaCleaningProducerImpl(final Producer<K, V> delegate,
            final Function<TopicPartition, ? extends Consumer<K, ?>> consumerCreator) {
        _delegate = delegate;
        _consumerCreator = consumerCreator;
    }

    @ParametersAreNonnullByDefault
    private static class Range {

        /**
         * the first offset of the refill
         */
        private volatile long _start;

        /**
         * the first offset which is after the refill
         */
        private volatile long _end;

        /**
         * constructor to create an empty range
         */
        Range(final long offset) {
            _start = offset;
            _end = offset;
        }

        /**
         * copy constructor
         */
        Range(final Range orig) {
            _start = orig._start;
            _end = orig._end;
        }

        /**
         * add a known record to the range
         */
        synchronized void update(final long offset) {
            _start = Math.min(_start, offset);
            _end = Math.max(_end, offset + 1);
        }

        @Override
        public String toString() {
            return _start + " to " + _end;
        }

    }

    /**
     * This callback is used to track the last offset of a refill record in a partition.
     *
     * @author Jan Burkhardt (initial creation)
     */
    @ParametersAreNonnullByDefault
    private final class RecognizeCallback implements Callback {

        private final Callback _callback;

        private RecognizeCallback(@Nullable final Callback callback) {
            _callback = callback;
        }

        @Override
        public void onCompletion(final RecordMetadata metadata, final Exception exception) {
            if (metadata != null) { // otherwise an exception occurred
                synchronized (_activeStates) { // synchronization needed see Multimaps#synchronizedMap
                    for (final KafkaCleaningProducerImplState state : _activeStates.get(metadata.topic())) {
                        state.recognize(metadata);
                    }
                }
            }
            if (_callback != null) {
                _callback.onCompletion(metadata, exception);
            }
        }
    }

    @Override
    public Future<RecordMetadata> send(final ProducerRecord<K, V> record) {
        return _delegate.send(record, _nullCallback);
    }

    @Override
    public Future<RecordMetadata> send(final ProducerRecord<K, V> record, final Callback callback) {
        return _delegate.send(record, callback(record, callback));
    }

    @CheckForNull
    private Callback callback(final ProducerRecord<K, V> record, @Nullable final Callback callback) {
        if (_activeStates.isEmpty() || !_activeStates.containsKey(record.topic())) { // topic is not interesting
            return callback;
        } else if (callback == null) { // no callback provided, safe memory
            return _nullCallback;
        } else {
            return new RecognizeCallback(callback);
        }
    }

    @Override
    public void flush() {
        _delegate.flush();
    }

    @Override
    public List<PartitionInfo> partitionsFor(final String topic) {
        return _delegate.partitionsFor(topic);
    }

    @Override
    public Map<MetricName, ? extends Metric> metrics() {
        return _delegate.metrics();
    }

    @Override
    public void close() {
        _delegate.close();
    }

    @Override
    public void close(final long timeout, final TimeUnit unit) {
        _delegate.close(timeout, unit);
    }

    @Override
    public KafkaCleaningProducerState<K> beginRefill(final Set<String> topics) {
        if (topics.isEmpty()) {
            throw new IllegalArgumentException("you have to specify at least one topic");
        }
        return new KafkaCleaningProducerImplState(topics);
    }

    @ParametersAreNonnullByDefault
    private class KafkaCleaningProducerImplState extends KafkaCleaningProducerState<K> {

        /**
         * noticed ranges where the refill data is written
         */
        private final ConcurrentMap<TopicPartition, Range> _ranges = new ConcurrentHashMap<>();

        KafkaCleaningProducerImplState(final Set<String> topics) {
            super(topics);
            for (final String topic : topics) {
                _activeStates.put(topic, this);
            }
        }

        @Nonnull
        Range getRange(final RecordMetadata metadata) {
            final TopicPartition topicPartition = new TopicPartition(metadata.topic(), metadata.partition());
            final Range range = _ranges.get(topicPartition);
            if (range != null) {
                return range;
            }

            _ranges.putIfAbsent(topicPartition, new Range(metadata.offset()));
            return _ranges.get(topicPartition);
        }

        public void recognize(final RecordMetadata metadata) {
            if (_topics.contains(metadata.topic())) {
                getRange(metadata).update(metadata.offset());
            }
        }

        @Override
        public void finishRefillTopicFilter(final Function<? super String, Predicate<? super K>> topicAndKeyPredicate) {
            // flush to have all refilled partitions to have an acceptable offset
            flush();

            // make a copy to prevent further changes
            final ImmutableMap<TopicPartition, Range> allRanges =
                    ImmutableMap.copyOf(Maps.transformValues(_ranges, Range::new));

            final Queue<Cleaner> cleaners = new LinkedList<>();

            for (final String topic : _topics) {
                for (final PartitionInfo partitionInfo : _delegate.partitionsFor(topic)) {
                    final TopicPartition topicPartition = new TopicPartition(topic, partitionInfo.partition());
                    final Consumer<K, ?> consumer = _consumerCreator.apply(topicPartition);
                    if (consumer != null) {
                        cleaners.add(new Cleaner(consumer, topicPartition, allRanges,
                                topicAndKeyPredicate.apply(topic)));
                    }
                }
            }

            //instead of calling them one by one a Executor could be used to execute the tasks
            cleaners.forEach(Cleaner::run);

            close();
        }

        @Override
        public void close() {
            _topics.forEach(topic -> _activeStates.remove(topic, this));
        }

    }

    @Nonnull
    public KafkaCleaningProducerImpl<K, V> setMaxSetSize(final int maxSetSize) {
        _maxSetSize = maxSetSize;
        return this;
    }

    @ParametersAreNonnullByDefault
    private final class Cleaner implements Runnable {

        private final Consumer<K, ?> _consumer;
        private final TopicPartition _topicPartition;
        private final Range _range;

        /**
         * the set of keys which need to be deleted
         */
        private final HashSet<K> _toDelete = new HashSet<>(1000);
        private final Predicate<? super K> _keyPredicate;

        private Cleaner(final Consumer<K, ?> consumer, final TopicPartition topicPartition,
                final ImmutableMap<TopicPartition, Range> allRanges, final Predicate<? super K> keyPredicate) {
            _consumer = consumer;
            _topicPartition = topicPartition;
            _keyPredicate = keyPredicate;
            _range = currentRange(allRanges);
        }

        @Nonnull
        private Range currentRange(final ImmutableMap<TopicPartition, Range> allOffsets) {
            final Range range = allOffsets.get(_topicPartition);
            if (range != null) {
                return range;
            }

            // if no record found, all entries are obsolete, create an empty range at the end of the queue
            _consumer.seekToEnd(ImmutableList.of(_topicPartition));
            return new Range(currentPosition());
        }

        @Override
        public void run() {
            try {
                //find the beginning offset and use it
                _consumer.seekToBeginning(ImmutableList.of(_topicPartition));
                long offset = currentPosition();

                //handle all record until we reach the start of our range
                while (offset < _range._start) {
                    offset = pollAndHandleRecords(offset);

                    //if set size is exceeded to do a clean run
                    if (_toDelete.size() > _maxSetSize) {
                        startClean(Math.max(_range._start, offset));
                    }
                }

                //range with valid records reached, start cleaning
                startClean(offset);

            } finally {
                if (_consumer != null) {
                    try {
                        _consumer.close();
                    } catch (final RuntimeException e) {
                        // ignore
                    }
                }
            }
        }

        private long currentPosition() {
            return _consumer.position(_topicPartition);
        }

        /**
         * Start cleaning.
         *
         * All keys in {@link #_toDelete} were deleted if they cannot be found inside the range.
         *
         * @param start
         *            the offset where to start reading the refilled entries, this might be behind {@link #_range}._start
         */
        private void startClean(final long start) {
            long offset = start;
            while (offset < _range._end && !_toDelete.isEmpty()) {
                offset = pollAndHandleRecords(offset);
            }

            // if we have entries to delete left, delete them
            if (!_toDelete.isEmpty()) {
                for (final K k : _toDelete) {
                    final Integer partition = Integer.valueOf(_topicPartition.partition());
                    //write through our self to prevent duplicate deletes
                    send(new ProducerRecord<>(_topicPartition.topic(), partition, k, null));
                }
                _toDelete.clear();
                //flush the producer to get the entries back in the consumer
                _delegate.flush();
            }
        }

        /**
         * Poll the consumer and handle the read records.
         *
         * Records which are deleted are valid, because, they don't need to be deleted again.
         * Records which are behind {@link #_range}._start are valid too.
         *
         * This method is also used for cleaning, where it is called with offset > {@link #_range}._start.
         *
         * @param offset
         *            where to start reading
         * @return offset of first element which was not read
         */
        private long pollAndHandleRecords(final long offset) {
            long newOffset = offset;
            _consumer.seek(_topicPartition, offset);
            // poll as short as possible to get our own entries
            final ConsumerRecords<K, ?> records = _consumer.poll(1);
            for (final ConsumerRecord<K, ?> record : records) {
                newOffset = Math.max(record.offset() + 1, newOffset);
                final K key = record.key();
                if (_keyPredicate.test(key)) {
                    // a record is valid if it is a delete or if it is in or behind the range
                    if (record.value() == null || record.offset() >= _range._start) {
                        _toDelete.remove(record.key());
                    } else {
                        _toDelete.add(record.key());
                    }
                }
            }
            return newOffset;
        }
    }

    @Override
    public void initTransactions() {
        _delegate.initTransactions();
    }

    @Override
    public void beginTransaction() throws ProducerFencedException {
        _delegate.beginTransaction();
    }

    @Override
    public void sendOffsetsToTransaction(final Map<TopicPartition, OffsetAndMetadata> offsets, final String consumerGroupId)
        throws ProducerFencedException {
        _delegate.sendOffsetsToTransaction(offsets, consumerGroupId);
    }

    @Override
    public void commitTransaction() throws ProducerFencedException {
        _delegate.commitTransaction();
    }

    @Override
    public void abortTransaction() throws ProducerFencedException {
        _delegate.abortTransaction();
    }

    /**
     * Utility method to create an appropriate consumer
     */
    @Nonnull
    public static <K> Consumer<K, ?> createConsumer(final TopicPartition topicPartition,
            final Deserializer<K> keyDeserializer,
            @Nullable final Properties consumerProps) {
        final KafkaConsumer<K, byte[]> result =
                new KafkaConsumer<>(consumerProps, keyDeserializer, new ByteArrayDeserializer());
        result.assign(ImmutableList.of(topicPartition));
        return result;
    }

    /**
     * Utility method to create an appropriate consumer
     */
    @Nonnull
    public static Consumer<String, ?> createStringConsumer(final TopicPartition topicPartition,
            @Nullable final Properties consumerProps) {
        return createConsumer(topicPartition, new StringDeserializer(), consumerProps);
    }

}
