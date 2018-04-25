package de.justsoftware.toolbox.performance;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.ConstructorSignature;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;

import de.justsoftware.toolbox.model.AbstractImmutableEqualsObject;

/**
 * This class provides static methods to log Performance and do the log output itself:
 * <ul>
 * <li>{@link #startMethod} or any other of the start methods to be called before the invocation of a method.</li>
 * <li>{@link PerformanceLoggerTimestamp#finish} to be called after invocation of a method, preferred in a finally block</li>
 * <li>{@link #logJoinPoint} to be used with aspectj</li>
 * </ul>
 */
public class PerformanceLogger {

    private static final double NANOS_PER_MILLI_DOUBLE = TimeUnit.MILLISECONDS.toNanos(1);

    private static final Logger LOG = LoggerFactory.getLogger(PerformanceLogger.class);

    private static volatile long _thresholdMethodNanos = TimeUnit.MICROSECONDS.toNanos(10);
    private static volatile long _thresholdTotalNanos = TimeUnit.MILLISECONDS.toNanos(100);
    private static volatile long _thresholdWarnNanos = TimeUnit.SECONDS.toNanos(1);

    private static boolean _performanceLoggerEnabled = false;

    private static volatile int _logCount = 0;

    /**
     * we don't output call trees with a depth more than this.
     */
    private static final int MAX_DEPTH = 100;

    /**
     * the stats are limited
     */
    private static final int MAX_STATS_COUNT = 100;

    private static final TotalCount OVERALL_COUNT = new TotalCount(new MethodDescription(PerformanceLogger.class, "total"));
    private static final ConcurrentHashMap<MethodDescription, TotalCount> TOTAL_COUNTS =
            new ConcurrentHashMap<>(ImmutableMap.of(OVERALL_COUNT._method, OVERALL_COUNT));

    /**
     * this threadlocal {@link Deque} is used for the Callstack for every thread.
     */
    private static final ThreadLocal<Deque<CallTreeNode>> STACK = ThreadLocal.withInitial(ArrayDeque::new);

    @ParametersAreNonnullByDefault
    static final class MethodDescription extends AbstractImmutableEqualsObject {

        private final String _signatureName;
        private final ImmutableList<Class<?>> _params;

        MethodDescription(final Class<?> clz, final Constructor<?> constructor) {
            this(clz, constructor.getName(), constructor.getParameterTypes());
        }

        MethodDescription(final Class<?> clz, final Method method) {
            this(clz, method.getName(), method.getParameterTypes());
        }

        MethodDescription(final Class<?> clz, final String name, final Class<?>... params) {
            this(clz.getSimpleName() + "." + name, ImmutableList.copyOf(params));
        }

        private MethodDescription(final String signatureName, final ImmutableList<Class<?>> params) {
            super(signatureName, params);
            _signatureName = signatureName;
            _params = params;
        }

        @Nonnull
        String createMethodSignature() {
            final StringBuilder sb = new StringBuilder(_signatureName).append("( ");
            Joiner.on(", ").appendTo(sb, _params.stream().map(Class::getSimpleName).iterator());
            return sb.append(" )").toString();
        }

    }

    /**
     * for faster output, we store the string for the tree in every possible depth.
     */
    private static final ImmutableList<String> DEPTH_STRING = IntStream
            .range(0, MAX_DEPTH)
            .mapToObj(depth -> {
                final StringBuilder sb = new StringBuilder();
                for (int i = 1; i < depth; i++) {
                    sb.append("| ");
                }
                if (depth > 0) {
                    sb.append("|-");
                }
                return sb.toString();
            })
            .collect(ImmutableList.toImmutableList());

    /**
     * Timestamp for performance logging, encapsulates current time, cpu time and user time.
     */
    @ParametersAreNonnullByDefault
    public static class PerformanceLoggerTimestamp {

        private static final ThreadMXBean THREAD_MX_BEAN = ManagementFactory.getThreadMXBean();
        private static final boolean CURRENT_THREAD_CPU_TIME_SUPPORTED = isCurrentThreadCpuTimeSupported();

        final long _systemNanoTime;
        final long _currentThreadCpuTime;
        final long _currentThreadUserTime;
        final PerformanceLogger.MethodDescription _method;

        PerformanceLoggerTimestamp(final PerformanceLogger.MethodDescription method) {
            _method = method;
            _systemNanoTime = System.nanoTime();
            if (CURRENT_THREAD_CPU_TIME_SUPPORTED) {
                _currentThreadCpuTime = THREAD_MX_BEAN.getCurrentThreadCpuTime();
                _currentThreadUserTime = THREAD_MX_BEAN.getCurrentThreadUserTime();
            } else {
                _currentThreadCpuTime = _systemNanoTime;
                _currentThreadUserTime = _systemNanoTime;
            }
        }

        private static boolean isCurrentThreadCpuTimeSupported() {
            if (THREAD_MX_BEAN.isCurrentThreadCpuTimeSupported()) {
                return true;
            }
            try {
                THREAD_MX_BEAN.setThreadCpuTimeEnabled(true);
            } catch (final RuntimeException e) {
                LOG.error("unable to setThreadCpuTimeEnabled: " + e.getMessage(), e);
            }
            final boolean result = THREAD_MX_BEAN.isCurrentThreadCpuTimeSupported();
            if (!result) {
                LOG.error("current thread cpu time is disabled, expect gaps in your performance log!");
            }
            return result;
        }

        /**
         * call this method after invocation of a method, preferred in a finally block.
         */
        @ParametersAreNonnullByDefault
        public void finish() {
            final PerformanceLoggerTimestamp end = new PerformanceLoggerTimestamp(_method);
            final Deque<CallTreeNode> stack = STACK.get();
            if (stack.isEmpty()) {
                throw new IllegalStateException("Stack is empty!");
            }
            final CallTreeNode stackedMethod = stack.removeLast();

            if (!Objects.equals(stackedMethod._method, _method)) {
                throw new IllegalStateException("Method on stack is not equals to given!");
            }
            stackedMethod.increment(this, end);
            if (!stack.isEmpty()) {
                return;
            }
            STACK.remove();
            if (!_performanceLoggerEnabled) {
                return;
            }

            stackedMethod.count();
            OVERALL_COUNT.increment(stackedMethod);
            if (stackedMethod._duration > _thresholdWarnNanos) {
                LOG.warn(stackedMethod.writeCallTree());
                incrementAndLogTotalCounts();
            } else if (stackedMethod._duration > _thresholdTotalNanos && LOG.isInfoEnabled()) {
                LOG.info(stackedMethod.writeCallTree());
                incrementAndLogTotalCounts();
            } else if (LOG.isDebugEnabled()) {
                LOG.debug(stackedMethod.writeCallTree());
                incrementAndLogTotalCounts();
            }
        }

    }

    /**
     * This class represents a node of the call tree and stores the accumulated duration and the invocation amount.
     */
    @ParametersAreNonnullByDefault
    private static class CallTreeNode {

        long _duration = 0;
        long _cpuDuration = 0;
        long _userDuration = 0;
        int _count = 0;
        final Map<MethodDescription, CallTreeNode> _children = new HashMap<>();
        final MethodDescription _method;

        CallTreeNode(final MethodDescription method) {
            _method = method;
        }

        void increment(final PerformanceLoggerTimestamp start, final PerformanceLoggerTimestamp end) {
            _count++;
            _duration += end._systemNanoTime - start._systemNanoTime;
            _cpuDuration += end._currentThreadCpuTime - start._currentThreadCpuTime;
            _userDuration += end._currentThreadUserTime - start._currentThreadUserTime;
        }

        long getDuration() {
            return _duration;
        }

        void count() {
            final MethodDescription method = _method;
            final TotalCount fromMap = TOTAL_COUNTS.get(method);
            final TotalCount totalCount;
            if (fromMap == null) {
                final TotalCount newTotalCount = new TotalCount(method);
                final TotalCount existing = TOTAL_COUNTS.putIfAbsent(method, newTotalCount);
                totalCount = existing != null
                    ? existing
                    : newTotalCount;
            } else {
                totalCount = fromMap;
            }
            totalCount.increment(this);
            _children.values().forEach(CallTreeNode::count);
        }

        @Nonnull
        @ParametersAreNonnullByDefault
        private StringBuilder writeCallTree(final StringBuilder sb, final int depth) {
            sb.append(String.format("%7dx %8.2f %8.2f %8.2f %s %s",
                    Long.valueOf(_count),
                    Double.valueOf(_duration / NANOS_PER_MILLI_DOUBLE),
                    Double.valueOf(_cpuDuration / NANOS_PER_MILLI_DOUBLE),
                    Double.valueOf(_userDuration / NANOS_PER_MILLI_DOUBLE),
                    DEPTH_STRING.get(depth),
                    _method.createMethodSignature()));

            if (_duration < _thresholdMethodNanos) {
                return sb.append(" - takes fewer time than threshold, skipping children\n");
            } else if (depth >= MAX_DEPTH - 1) {
                return sb.append(" - too deep, skipping children\n");
            }
            sb.append("\n");

            _children
                    .values()
                    .stream()
                    .sorted(Comparator.comparing(CallTreeNode::getDuration).reversed())
                    .forEachOrdered(child -> child.writeCallTree(sb, depth + 1));
            return sb;
        }

        @Nonnull
        String writeCallTree() {
            return writeCallTree(new StringBuilder("   count time[ms]  cpu[ms] user[ms] method\n"), 0).toString();
        }

    }

    @ParametersAreNonnullByDefault
    private static final class TotalCount {

        final MethodDescription _method;

        final AtomicLong _duration = new AtomicLong();
        final AtomicLong _cpuDuration = new AtomicLong();
        final AtomicLong _userDuration = new AtomicLong();
        final AtomicInteger _count = new AtomicInteger();

        TotalCount(final MethodDescription method) {
            _method = method;
        }

        void increment(final CallTreeNode callTreeNode) {
            _count.addAndGet(callTreeNode._count);
            _duration.addAndGet(callTreeNode._duration);
            _cpuDuration.addAndGet(callTreeNode._cpuDuration);
            _userDuration.addAndGet(callTreeNode._userDuration);
        }

        long getDuration() {
            return _duration.get();
        }

    }

    @Nonnull
    @ParametersAreNonnullByDefault
    private static CallTreeNode createOrGetCurrent(final Deque<CallTreeNode> stack, final MethodDescription method) {
        if (stack.isEmpty()) {
            return new CallTreeNode(method);
        }

        final CallTreeNode parent = stack.getLast();
        final CallTreeNode timer = parent._children.get(method);
        if (timer != null) {
            return timer;
        }
        final CallTreeNode newTimer = new CallTreeNode(method);
        parent._children.put(method, newTimer);
        return newTimer;
    }

    private static void incrementAndLogTotalCounts() {
        if (++_logCount <= 10) { // log only every 10th
            return;
        }
        _logCount = 0;

        final StringBuilder sb = new StringBuilder("stats:\n   count  time[ms] /call[ms]   cpu[ms]  user[ms] method\n");

        final ImmutableList<TotalCount> orderedByDuration =
                Ordering.natural().reverse().onResultOf(TotalCount::getDuration)
                        .immutableSortedCopy(TOTAL_COUNTS.values());
        final Iterable<TotalCount> limited = Iterables.limit(orderedByDuration, MAX_STATS_COUNT);
        for (final TotalCount methodCall : limited) {
            final double duration = methodCall._duration.get() / NANOS_PER_MILLI_DOUBLE;
            final int count = methodCall._count.get();
            sb.append(String.format("%7dx %9.2f %9.2f %9.2f %9.2f %s%n",
                    Long.valueOf(count),
                    Double.valueOf(duration),
                    Double.valueOf(duration / count),
                    Double.valueOf(methodCall._cpuDuration.get() / NANOS_PER_MILLI_DOUBLE),
                    Double.valueOf(methodCall._userDuration.get() / NANOS_PER_MILLI_DOUBLE),
                    methodCall._method.createMethodSignature()));

        }
        LOG.warn(sb.toString());

    }

    @Nonnull
    @ParametersAreNonnullByDefault
    private static PerformanceLoggerTimestamp startMethod(final MethodDescription method) {
        final Deque<CallTreeNode> stack = STACK.get();
        stack.addLast(createOrGetCurrent(stack, method));
        return new PerformanceLoggerTimestamp(method);
    }

    // --- custom invocations

    /**
     * call this method before invocation of a method. the method(name) is supplied by a reflection object.
     *
     * @return time of start, to be used for calling {@link PerformanceLoggerTimestamp#finish}
     */
    @Nonnull
    @ParametersAreNonnullByDefault
    public static PerformanceLoggerTimestamp startMethod(final Class<?> clz, final Method method) {
        return startMethod(new MethodDescription(clz, method));
    }

    /**
     * call this method before invocation of a method. the method(name) is supplied as string.
     *
     * @return time of start, to be used for calling {@link PerformanceLoggerTimestamp#finish}
     */
    @Nonnull
    @ParametersAreNonnullByDefault
    public static PerformanceLoggerTimestamp startMethod(final Class<?> clz, final String method,
            final Class<?>... params) {
        return startMethod(new MethodDescription(clz, method, params));
    }

    /**
     * call this method before invocation of a method. the method(name) is supplied as aspectj signature object.
     *
     * @return time of start, to be used for calling {@link PerformanceLoggerTimestamp#finish}
     */
    @Nonnull
    @ParametersAreNonnullByDefault
    public static PerformanceLoggerTimestamp startMethod(final Signature signature) {
        if (signature instanceof MethodSignature) {
            return startMethod(signature.getDeclaringType(), ((MethodSignature) signature).getMethod());
        } else if (signature instanceof ConstructorSignature) {
            return startConstructor(signature.getDeclaringType(), ((ConstructorSignature) signature).getParameterTypes());
        } else {
            return startMethod(signature.getDeclaringType(), signature.getName());
        }
    }

    /**
     * call this method before invocation of a constructor. the method(name) is derived from the class name.
     *
     * @return time of start, to be used for calling {@link PerformanceLoggerTimestamp#finish}
     */
    @Nonnull
    @ParametersAreNonnullByDefault
    public static PerformanceLoggerTimestamp startConstructor(final Class<?> clz, final Class<?>... params) {
        try {
            return startMethod(new MethodDescription(clz, clz.getConstructor(params)));
        } catch (final SecurityException | NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    // --- configuration methods

    /**
     * enable or disable the performance logger.
     */
    public static void setPerformanceLoggerEnabled(final boolean enabled) {
        _performanceLoggerEnabled = enabled;
    }

    /**
     * single method invocations below this limit aren't logged to reduce the size of the call trees
     */
    public static void setThresholdMethodNanos(final long thresholdMethodNanos) {
        _thresholdMethodNanos = thresholdMethodNanos;
    }

    /**
     * requests below this limit are logged to debug, defaults to 100ms
     */
    public static void setThresholdTotalNanos(final long thresholdTotalNanos) {
        _thresholdTotalNanos = thresholdTotalNanos;
    }

    /**
     * requests above this limit are logged on warn level, others use info, defaults to 1s
     */
    public static void setThresholdWarnNanos(final long thresholdWarnNanos) {
        _thresholdWarnNanos = thresholdWarnNanos;
    }

    // --- aop methods

    /**
     * directly invoke this method in your aspect
     */
    //CSOFF: IllegalThrows|Jsr305Annotations Throwable is declared correctly 
    // and nullness annotations would be weaved to classes and will result in findbugs errors
    public static Object logJoinPoint(final ProceedingJoinPoint joinPoint) throws Throwable {
        //CSON: .
        if (!_performanceLoggerEnabled) {
            return joinPoint.proceed();
        }
        final PerformanceLoggerTimestamp start = startMethod(joinPoint.getSignature());
        try {
            return joinPoint.proceed();
        } finally {
            start.finish();
        }
    }

}
